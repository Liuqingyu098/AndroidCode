package com.example.mapphotos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapphotos.bean.Picture;
import com.example.mapphotos.bean.RowInfoBean;
import com.example.mapphotos.util.CommonUtils;
import java.util.ArrayList;
import java.util.List;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class MainActivity extends AppCompatActivity {

    public static final int TO = 11; // 请求码
    public static final int BACK = 12; // 返回码

    private ListView photoListView;
    private PopupWindow popupWindow;
    private TextView textView;
    private Button btnGallery;
    private Button btnMenu;
    private PhotoAdapter photoAdapter;
    private DBOpenHelper dbOpenHelper;
    private List<Picture> pictures = new ArrayList<>();
    private List<RowInfoBean> photoList = new ArrayList<RowInfoBean>();
    private int selectedRowIndex = -1;// 长按选中list的某一行，默认值为-1，表示取消选中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SQLiteStudioService.instance().start(this);
        textView = (TextView) findViewById(R.id.app_title);
        btnGallery = (Button) findViewById(R.id.main_top_gallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int albumId = -1;
                String title = "全部照片";
                RowInfoBean bean;
                // 如果选中了数据行，则得到对应相册的 id。
                // 注意：selectedRowIndex 是 ListView 的数据行号，不是相册 id
                if (selectedRowIndex != -1) {
                    bean = photoList.get(selectedRowIndex);
                    albumId = bean.getId();
                    title = bean.getTitle();
                }
                // 启动相册浏览，同时将相册 id 传递过去
                Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
                intent.putExtra("album_id", albumId);
                intent.putExtra("album_title", title);
                startActivity(intent);
            }
        });
        btnMenu = (Button) findViewById(R.id.main_top_menu);
        dbOpenHelper = new DBOpenHelper(this);
        pictures = dbOpenHelper.getAllPicture();
        for (int i = 0; i < pictures.size(); i++) {
            if (!CommonUtils.pictureIsExists(pictures.get(i).getPictureName())) {
                dbOpenHelper.deletePictureById(pictures.get(i).getId());
            }
        }

        // 从数据库获取相册以便ListView 组件显示
        photoList.clear();
        loadAlbumFromDB();
        // 初始化 ListView 组件，设定其 Adapter 以便加载数据行显示
        photoListView = (ListView)findViewById(R.id.photoListView);
        photoAdapter = new PhotoAdapter(this, photoList);
        photoListView.setAdapter(photoAdapter);
//        photoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // 获取所点击条目所在下标相册的数据
//                RowInfoBean rowInfoBean = photoList.get(position);
//                Toast.makeText(getApplicationContext(),"成功", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
//                // 将相册 id 和 title 传递给GalleryActivity
//                intent.putExtra("album_id", rowInfoBean.getId());
//                startActivity(intent);
//            }
//        });

        photoListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedRowIndex == position) {
                    selectedRowIndex = -1;
                } else{
                    selectedRowIndex = position;
                }
                // 将下标值传给photoAdapter
                photoAdapter.setSelectedRowIndex(selectedRowIndex);
                // 通知 List 更新显示
                photoAdapter.notifyDataSetChanged();
                // 返回 true 即不再做后续的长按处理
                return true;
            }
        });
    }

    public void OnMenu(View view){
        // 获取自定义的菜单布局文件
        View popupWindow_view = getLayoutInflater().inflate(R.layout.activity_main_top_menu, null,false);
        // 创建PopupWindow实例,设置菜单宽度和高度为包裹其自身内容
        popupWindow = new PopupWindow(popupWindow_view, 400,
                420, true);
        //设置菜单显示在按钮的下面
        popupWindow.showAsDropDown(findViewById(R.id.main_top_menu),0,0);

        //点击发起群聊功能
        LinearLayout btnAdd = (LinearLayout) popupWindow_view.findViewById(R.id.ll_popmenu_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //如果菜单存在并且为显示状态，就关闭菜单并初始化菜单
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                    // 创建输入框控件，设定输入数据为文本
                    final EditText textTitle = new EditText(MainActivity.this);
                    textTitle.setInputType(InputType.TYPE_CLASS_TEXT);
                    // 动态创建对话框
                    AlertDialog.Builder builder_add = new AlertDialog.Builder(MainActivity.this);
                    builder_add.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String title = textTitle.getText().toString();
                            // 将新增相册保存到数据库
                            dbOpenHelper.addAlbum(title);
                            // 重新加载数据库数据显示
                            photoList.clear();
                            loadAlbumFromDB();
                            photoAdapter.notifyDataSetChanged();
                        }
                    });
                    builder_add.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    // 设定对话框标题和界面，然后显示
                    builder_add.setTitle("新相册名称");
                    builder_add.setView(textTitle);
                    AlertDialog dialog = builder_add.create();
                    dialog.show();
                }
            }
        });

        LinearLayout btnRename = (LinearLayout) popupWindow_view.findViewById(R.id.ll_popmenu_rename);
        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果菜单存在并且为显示状态，就关闭菜单并初始化菜单
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                    if (selectedRowIndex != -1) {
                        // 获取当前选中行信息
                        final RowInfoBean bean = photoList.get(selectedRowIndex);
                        // 设定输入相册名称的输入框，默认值是当前相册名称
                        final EditText input = new EditText(MainActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        input.setText(bean.getTitle());
                        // 动态创建对话框
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setNegativeButton("修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bean.setTitle(input.getText().toString());
                                dbOpenHelper.updateAlbumTitle(bean.getId(), bean.getTitle());
                            }
                        });
                        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        // 设定对话框标题和界面，然后显示对话框
                        builder.setTitle("修改相册名称");
                        builder.setView(input);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        selectedRowIndex = -1;
                        photoAdapter.setSelectedRowIndex(selectedRowIndex);
                        photoAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getApplicationContext(), "请先长按数据行选中，再执行修改操作", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        LinearLayout btnDelete = (LinearLayout) popupWindow_view.findViewById(R.id.ll_popmenu_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果菜单存在并且为显示状态，就关闭菜单并初始化菜单
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                    if (selectedRowIndex != -1) {
                        // 删除被选中的数据行
                        int id = photoList.get(selectedRowIndex).getId();
                        pictures = dbOpenHelper.getPictureByAlbumId(id);
                        for (int i = 0; i < pictures.size(); i++) {
                            CommonUtils.deletePicture(pictures.get(i).getPictureName());
                        }
                        dbOpenHelper.deletePictureByAlbumId(id);
                        dbOpenHelper.deleteAlbum(id);
                        // 重置选中下标值
                        selectedRowIndex = -1;
                        photoAdapter.setSelectedRowIndex(selectedRowIndex);
                        // 更新 ListView 显示
                        photoAdapter.notifyDataSetInvalidated();
                        loadAlbumFromDB();
                    } else {
                        Toast.makeText(getApplicationContext(), "请先长按数据行选中，再执行删除操作", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void loadAlbumFromDB() {
        SQLiteDatabase db = openOrCreateDatabase("mapphotos.db", Context.MODE_PRIVATE, null);
        // 设定默认的缩略图
        Drawable defaultThumb = getResources().getDrawable(R.drawable.gallery);
        // 执行表查询获取所有的相册
        Cursor cursor = db.rawQuery("select * from t_album", null);
        while (cursor.moveToNext()) {
            RowInfoBean rowInfoBean = new RowInfoBean();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            // 处理缩略图
            String thumb = cursor.getString(cursor.getColumnIndex("thumb"));
            rowInfoBean.setId(id);
            rowInfoBean.setTitle(title);
            if (thumb == null || thumb.equals("")) {
                rowInfoBean.setThumb(defaultThumb);
            } else {
                // 处理缩略图的完整文件路径
                thumb = CommonUtils.THUMB_PATH + thumb;
                rowInfoBean.setThumb(new BitmapDrawable(getResources(), BitmapFactory.decodeFile(thumb)));
            }
            photoList.add(rowInfoBean);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case BACK:
                // 返回此界面时重新加载一次相册目录
                photoList.clear();
                loadAlbumFromDB();
                // 更新 ListView 组件显示
                photoAdapter.notifyDataSetChanged();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
