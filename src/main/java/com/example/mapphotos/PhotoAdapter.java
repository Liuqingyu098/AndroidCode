package com.example.mapphotos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mapphotos.bean.Picture;
import com.example.mapphotos.bean.RowInfoBean;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Picture> pictures;
    private List<RowInfoBean> photoList = new ArrayList<RowInfoBean>();
    private int selectedRowIndex = -1;// 长按选中list的某一行，默认值为-1，表示取消选中

    PhotoAdapter(Context context, List<RowInfoBean> photoList) {
        super();
        this.context = context;
        this.photoList = photoList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return photoList.size();
    }

    @Override
    public Object getItem(int position) {
        return photoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * ListView在显示数据行时会不断调用getView()获取界面显示组件对象
     * @param position
     * @param convertView
     * @param parent
     * @return convertView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 如果没有复用的行界面，则动态加载一个“行”布局进来
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.activity_main_listview_row,null);
        }
        // 获取第position行显示的数据项
        final RowInfoBean rowInfoBean = photoList.get(position);
        // 初始化“行”布局中的组件，并设置其显示内容
        ImageView thumbView = (ImageView)convertView.findViewById(R.id.imageViewThumb);
        TextView titleView = (TextView)convertView.findViewById(R.id.textViewTitle);
        TextView numView = (TextView) convertView.findViewById(R.id.textViewNum);
        thumbView.setBackgroundDrawable(rowInfoBean.getThumb());
        titleView.setText(rowInfoBean.getTitle());
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
        pictures = dbOpenHelper.getPictureByAlbumId(rowInfoBean.getId());
        numView.setText(pictures.size() + " 张");
        // 被选中行的高亮显示（改变其背景颜色）
        if (this.getSelectedRowIndex() == position) {
            convertView.setBackgroundColor(Color.parseColor("#63B8FF"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#F0F8FF"));
        }
        ImageView imageViewMap = (ImageView)convertView.findViewById(R.id.imageViewMap);
        imageViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapViewActivity.class);
                // 将相册 id 和 title 传递给MapViewActivity
                intent.putExtra("album_id", rowInfoBean.getId());
                intent.putExtra("album_title", rowInfoBean.getTitle());
                ((MainActivity)context).startActivityForResult(intent, MainActivity.TO);
            }
        });
        // 返回“行”布局视图给ListView显示
        return convertView;
    }

    public int getSelectedRowIndex() {
        return selectedRowIndex;
    }

    public void setSelectedRowIndex(int selectedRowIndex) {
        this.selectedRowIndex = selectedRowIndex;
    }

}
