package de.alfingo.al_welcome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The activity that displays the intro slider, this class can be extended to create an activity
 * with your own logic, just call the createWelcomeSlides from
 *
 * @author Rafael Miranda
 * @since 25.09.2017
 */
public class WelcomeActivity extends AppCompatActivity {

    ViewPager mSlidesViewPager;

    LinearLayout mDotsLinearLayout;

    Button mNextButton;

    Button mSkipButton;

    Drawable[] mSlidesImage;

    String[] mSlidesTitle;

    String[] mSlidesDescription;

    SliderViewPagerAdapter mSlidesAdapter;

    /*
      * For now it is darker gray and black
      */
    int colorInactiveDot;
    int colorActiveDot;

    /**
     * Launches the main activity of your app, set it to any activity you want to launch right
     * after the tutorial.
     */
    private void launchAndFinish() {
        // todo remove this line after changing which activity should be launched.
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(getString(R.string.pref_first_start), false)
                .apply();
        // todo: Launch any activity you want from here.
        Intent launchMainIntent = new Intent(this, WelcomeActivity.class);
        startActivity(launchMainIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If this is not the first start we just launch the next activity without even setting
        // the content view.
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(getString(R.string.pref_first_start), false)) {
            launchAndFinish();
        }

        setContentView(R.layout.activity_welcome);
        mSlidesViewPager = findViewById(R.id.welcome_view_pager);
        mDotsLinearLayout = findViewById(R.id.welcome_layoutDots);
        mNextButton = findViewById(R.id.welcome_btn_next);
        mSkipButton = findViewById(R.id.welcome_btn_skip);

        // getting our elements
        Resources res = getResources();
        colorInactiveDot = ResourcesCompat.getColor(res, android.R.color.darker_gray, null);
        colorActiveDot = ResourcesCompat.getColor(res, android.R.color.black, null);
        TypedArray typedImages = res.obtainTypedArray(R.array.welcome_images);
        mSlidesImage = new Drawable[typedImages.length()];
        for (int i = 0; i < typedImages.length(); i++)
            mSlidesImage[i] = typedImages.getDrawable(i);
        typedImages.recycle();
        mSlidesTitle = res.getStringArray(R.array.welcome_title);
        mSlidesDescription = res.getStringArray(R.array.welcome_description);
        if (mSlidesImage.length != mSlidesDescription.length || mSlidesDescription.length != mSlidesTitle.length)
            throw new IllegalArgumentException("All the welcome arrays should be the same size.");

        // now making things look pretty
        addBottomDots(0);
        mSlidesAdapter = new SliderViewPagerAdapter();
        mSlidesViewPager.setAdapter(mSlidesAdapter);
        mSlidesViewPager.addOnPageChangeListener(createOnPageChangeListener());

        // setting up the buttons
        mNextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int next = mSlidesViewPager.getCurrentItem() + 1;
                if (next < mSlidesDescription.length)
                    mSlidesViewPager.setCurrentItem(next);
                else {
                    preferences.edit().putBoolean(getString(R.string.pref_first_start), false)
                            .apply();
                    launchAndFinish();
                }
            }
        });

        mSkipButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                preferences.edit().putBoolean(getString(R.string.pref_first_start), false)
                        .apply();
                launchAndFinish();
            }
        });
    }

    /**
     * Every time the user changes a page the dots on the bottom and the buttons
     * should update accordingly.
     *
     * @return the created page change listener
     */
    private ViewPager.OnPageChangeListener createOnPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);

                // changing the text on the buttons
                if (position == mSlidesDescription.length - 1) {
                    mNextButton.setText(getString(R.string.got_it));
                    mSkipButton.setVisibility(View.GONE);
                } else {
                    mNextButton.setText(R.string.next);
                    mSkipButton.setVisibility(View.VISIBLE);
                }
            }

        };
    }

    /**
     * Adds and changes the color of the dots according to the page.
     *
     * @param currentPage which page we are currently in.
     */
    private void addBottomDots(int currentPage) {
        TextView[] dots = new TextView[mSlidesDescription.length];
        mDotsLinearLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dots[i].setText(Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                dots[i].setText(Html.fromHtml("&#8226;"));
            }
            dots[i].setTextSize(35);
            if (i == currentPage)
                dots[i].setTextColor(colorActiveDot);
            else
                dots[i].setTextColor(colorInactiveDot);
            mDotsLinearLayout.addView(dots[i]);
        }
    }

    /**
     * An adapter for the slides to be displayed with their proper images and
     * text.
     *
     * @author Rafael Miranda
     * @since 28.03.2017
     */
    private class SliderViewPagerAdapter extends PagerAdapter {

        SliderViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = container.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View itemView = inflater.inflate(R.layout.item_welcome_slide, container, false);
            TextView title = itemView.findViewById(R.id.item_welcome_title);
            TextView description = itemView.findViewById(R.id.item_welcome_description);
            ImageView image = itemView.findViewById(R.id.item_welcome_image);
            title.setText(mSlidesTitle[position]);
            description.setText(mSlidesDescription[position]);
            image.setImageDrawable(mSlidesImage[position]);
            container.addView(itemView);
            return itemView;
        }

        @Override
        public int getCount() {
            return mSlidesImage.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == null && object == null) || (view != null && view.equals(object));
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

    }


}
