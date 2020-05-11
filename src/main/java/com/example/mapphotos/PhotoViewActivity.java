package com.example.mapphotos;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.mapphotos.bean.Picture;
import com.example.mapphotos.util.CommonUtils;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.util.ArrayList;

public class PhotoViewActivity extends FragmentActivity {
    private ViewPager viewPager;
    private DBOpenHelper dbOpenHelper;
    private TextView pageTextView;
    private ArrayList<Picture> pictures;
    private ViewPagerAdapter viewPagerAdapter;
    private static int pageNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        dbOpenHelper = new DBOpenHelper(this);
        int position = getIntent().getIntExtra("position",0);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pageTextView = (TextView) findViewById(R.id.page);
        pictures = (new AlbumActivity()).getPictures(getIntent().getIntExtra("album_id",-1), dbOpenHelper);
        pageNum = pictures.size();
        pageTextView.setText(position + 1 + " / " + pageNum);
        viewPagerAdapter = new ViewPagerAdapter(this, pictures);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pageTextView.setText(position + 1 + " / " + pageNum);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    protected class ViewPagerAdapter extends PagerAdapter {
        private ArrayList<Picture> pictures;
        private Context context;
        private ViewGroup viewGroup;

        public ViewPagerAdapter(Context context, ArrayList<Picture> pictures) {
            this.context = context;
            this.pictures = pictures;
        }
        @Override
        public int getCount() {
            return pictures.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (viewGroup == null)
                viewGroup = container;

            //使图片具有放缩功能
            PhotoView photoView = new PhotoView(PhotoViewActivity.this);
            Glide.with(context)
                    .load(CommonUtils.PICTURE_PATH + pictures.get(position).getPictureName())
                    .into(photoView);

            container.addView(photoView);
            return photoView;
        }

    }
}
