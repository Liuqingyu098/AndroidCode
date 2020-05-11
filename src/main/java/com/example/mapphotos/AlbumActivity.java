package com.example.mapphotos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mapphotos.bean.Picture;
import com.example.mapphotos.util.CommonUtils;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropSquareTransformation;

public class AlbumActivity extends Activity {
    private RecyclerView recyclerView;
    private ImageView albumBack;
    private TextView albumText;
    private DBOpenHelper dbOpenHelper;
    private ArrayList<Picture> pictures;
    private RecyclerAdapter recyclerAdapter;
    private static final int column = 3;
    private static int albumId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        albumBack = (ImageView) findViewById(R.id.albumBack);
        albumText = (TextView) findViewById(R.id.albumText);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        dbOpenHelper = new DBOpenHelper(this);
        albumId = getIntent().getIntExtra("album_id",-1);
        recyclerAdapter = new RecyclerAdapter(AlbumActivity.this, getPictures(albumId, dbOpenHelper));
        // 设置布局管理器
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.findLastVisibleItemPositions(null);
        RecyclerItemDecoration itemDecoration = new RecyclerItemDecoration(32);
        recyclerView.addItemDecoration(itemDecoration);
        // 设置增加或删除时动画效果
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置adapter
        recyclerView.setAdapter(recyclerAdapter);
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                if (!recyclerView.canScrollVertically(-1)) {
//                    mWebOperation.updatePage();
//                }
//            }
//        });
        albumBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        albumText.setText(getIntent().getStringExtra("album_title"));
    }

    public ArrayList<Picture> getPictures(int albumId, DBOpenHelper dbOpenHelper) {
        if (albumId == -1) {
            pictures = dbOpenHelper.getAllPicture();
        } else {
            pictures = dbOpenHelper.getPictureByAlbumId(albumId);
        }
        return pictures;
    }

    protected class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
        private Context adpatercontext;
        private ArrayList<Picture> pictures;
        private LayoutInflater layoutInflater;
        public RecyclerAdapter(Context context, ArrayList<Picture> pictures) {
            this.adpatercontext = context;
            this.pictures = pictures;
            this.layoutInflater = LayoutInflater.from(context);
        }

        public void updateData(ArrayList<Picture> data) {
            this.pictures = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_album_item, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerAdapter.RecyclerViewHolder holder, final int position) {
            RequestOptions options = new RequestOptions().placeholder(R.drawable.gallery);
            String absolutePath = CommonUtils.PICTURE_PATH + pictures.get(position).getPictureName();
            Glide.with(adpatercontext)
                    .load(absolutePath)
                    .transform(new CropSquareTransformation())
                    .apply(options)
                    .into(holder.imageView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AlbumActivity.this, PhotoViewActivity.class);
                    intent.putExtra("album_id", albumId);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAlertDialog(position);
                    return true;
                }
            });
        }

        public void showAlertDialog(final int position) {
            // 动态创建对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this);
            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CommonUtils.deletePicture(pictures.get(position).getPictureName());
                    dbOpenHelper.deletePictureById(pictures.get(position).getId());
                    pictures.remove(position);
                    notifyItemRemoved(position);
                }
            });
            builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // 设定对话框标题和界面，然后显示对话框
            builder.setTitle("提示: ");
            builder.setMessage("确定要删除该照片吗？");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        public int getItemCount() {
            return pictures.size();
        }

        class RecyclerViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            RecyclerViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.itemImageView);
            }
        }
    }

    private class RecyclerItemDecoration extends RecyclerView.ItemDecoration {

        private int leftSpace;
        private int midSpace;
        private int rightSpace;

        public RecyclerItemDecoration(int leftSpace, int midSpace, int rightSpace) {
            this.leftSpace = leftSpace;
            this.midSpace = midSpace;
            this.rightSpace = rightSpace;
        }

        public RecyclerItemDecoration(int space) {
            this.leftSpace = space;
            this.midSpace = space;
            this.rightSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                int spanCount = gridLayoutManager.getSpanCount();
                int totalSpace = leftSpace + rightSpace + midSpace * (spanCount - 1);
                int itemNeedSpace = totalSpace / spanCount;
                if (parent.getChildAdapterPosition(view) % spanCount == 0) {
                    //最左一条
                    outRect.left = leftSpace;
                    outRect.right = itemNeedSpace - leftSpace;
                } else if (parent.getChildAdapterPosition(view) % spanCount == spanCount - 1) {
                    //最右一条
                    outRect.left = itemNeedSpace - rightSpace;
                    outRect.right = rightSpace;
                } else {
                    outRect.left = itemNeedSpace / 2;
                    outRect.right = itemNeedSpace / 2;
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        // 按<back>键时返回给前一个 Activity 的结果码
        setResult(MainActivity.BACK);
        finish();
    }
}

