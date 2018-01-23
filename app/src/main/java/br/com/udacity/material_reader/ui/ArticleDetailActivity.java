package br.com.udacity.material_reader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import br.com.udacity.material_reader.R;
import br.com.udacity.material_reader.data.ArticleLoader;
import br.com.udacity.material_reader.data.ItemsContract;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    private MyPagerAdapter mPagerAdapter;

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.detail_toolbar_layout)
    CollapsingToolbarLayout mAppBarLayout;

    @BindView(R.id.photo_view)
    ImageView mPhotoView;

    @BindView(R.id.pager)
    ViewPager mPager;

    @BindView(R.id.share_fab)
    FloatingActionButton mShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        //Setting up ButterKnife
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());

        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    Timber.i(mCursor.getString(ArticleLoader.Query.THUMB_URL));
                    mAppBarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

                    Picasso.with(getBaseContext())
                            .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                            .placeholder(R.drawable.photo_background_protection)
                            .error(R.drawable.ic_error_outline)
                            .into(mPhotoView);
                }
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();

            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }

            Timber.i(mCursor.getString(ArticleLoader.Query.THUMB_URL));

            Picasso.with(this)
                    .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                    .placeholder(R.drawable.photo_background_protection)
                    .error(R.drawable.ic_error_outline)
                    .into(mPhotoView);

            if (mAppBarLayout != null) {
                mAppBarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            }

            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.share_fab)
    public void onShareFabClicked() {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                .setType(getString(R.string.text_type))
                .setText(mCursor.getString(ArticleLoader.Query.TITLE) + " " + getString(R.string.text_share))
                .getIntent(), getString(R.string.action_share)));
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
