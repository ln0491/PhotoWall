package com.liu.photowall;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liu.photowall.adapter.PhotoAdapter;
import com.liu.photowall.bean.PhotoFolder;
import com.liu.photowall.view.PopuListImageWindow;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoWallActivity extends AppCompatActivity implements PhotoAdapter.OnItemClickListener, PopuListImageWindow.OnPopuWindowItemClickListen {

    private static final int LAST_PHOTO = 1;
    private static final int ALL        = 2;
    @Bind(R.id.ivTitleLeftClose)
    ImageButton    mIvTitleLeftClose;
    @Bind(R.id.tvTitle)
    TextView       mTvTitle;
    @Bind(R.id.tvTitleRight)
    TextView       mTvTitleRight;
    @Bind(R.id.tvPathName)
    TextView       mTvPathName;
    @Bind(R.id.tvPhotoNum)
    TextView       mTvPhotoNum;
    @Bind(R.id.rlBottom)
    RelativeLayout mRlBottom;
    @Bind(R.id.photoRecylerView)
    RecyclerView   mPhotoRecylerView;


    //View mBottom ;

    private ProgressDialog mProgressDialog;

    /**
     * 最近使用的的照片
     */
    ArrayList<String> MLatestImagePaths = new ArrayList<>();
    private int                    mMaxtCount    = 4;
    private ArrayList<PhotoFolder> mPhotoFolders = new ArrayList<>();
    private File         mCurrentFile;
    private PhotoAdapter mPhotoAdapter;


    private static final String LAST_USER_PHOTO = "最近使用的照片";
    private List<String>        mPhotoLists;
    private PopuListImageWindow mPopuListImageWindow;

    private int maxNum =4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
        initData();
        initListener();
    }

    private void initView() {


    }


    private void initData() {
        mProgressDialog = ProgressDialog.show(this, "请稍后...", "正在加载中.....");
        //获取最近的100张照片
        getLatestImagePaths(100);

        getAllImagePath();


        PhotoFolder LastPhotoFolder = new PhotoFolder("last_user_photo", LAST_USER_PHOTO, MLatestImagePaths.get(0), MLatestImagePaths.size());

        mPhotoFolders.add(0, LastPhotoFolder);

        initRecyclerView();
        mProgressDialog.dismiss();
    }


    private void initRecyclerView() {


        mTvPathName.setText(LAST_USER_PHOTO);
        mTvPhotoNum.setText(MLatestImagePaths.size() + "");

        mPhotoAdapter = new PhotoAdapter(this, MLatestImagePaths, "", true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(PhotoWallActivity.this, 3);

        mPhotoRecylerView.setLayoutManager(gridLayoutManager);

        mPhotoRecylerView.setAdapter(mPhotoAdapter);
        mPhotoAdapter.setmOnItemClickListener(this);
        List<String> selectedList = mPhotoAdapter.getSelectedList();
        mTvTitleRight.setText("确定 ("+selectedList.size()+"/"+maxNum+")");
    }

    /**
     * 获取所有的图片也文件夹
     */
    private void getAllImagePath() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //判断外部存储卡是否存在
            Toast.makeText(this, "外部存储卡不可用", Toast.LENGTH_LONG).show();
            return;
        }

        //获取URI
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = PhotoWallActivity.this.getContentResolver();

        Cursor cursor = contentResolver.query(uri, null,//查询URI，所有列
                MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ? ", //过滤条件 只查jpeg,jpg,png
                new String[]{"image/jpeg", "image/png", "image/jpg"}, //条件
                MediaStore.Images.Media.DATE_MODIFIED);//排序修改时间


        //防止重复扫描文件夹
        HashSet<String> mDirPath = new HashSet<String>();
        while (cursor.moveToNext()) {
            //获取图片路径-列索引
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            //获取父文件
            File parentFile = new File(path).getParentFile();
            //判断是否为NULL
            if (parentFile == null) {
                continue;
            }

            //获取父文件夹路径
            String      dirPath     = parentFile.getAbsolutePath();
            PhotoFolder photoFolder = null;


            if (mDirPath.contains(dirPath)) {
                continue;
            } else {
                //
                mDirPath.add(dirPath);

                photoFolder = new PhotoFolder();
                photoFolder.setPath(dirPath);

                photoFolder.setFirstImagePath(path);

            }

            if (parentFile.list() == null) {
                continue;
            }

            //文件夹下的图片数量
            int picSize = parentFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {

                    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }).length;

            photoFolder.setImageCount(picSize);

            //放在集合中
            mPhotoFolders.add(photoFolder);


            if (picSize > mMaxtCount) {
                mMaxtCount = picSize;
                //当前文件夹就是当前路径的父文件夹
                mCurrentFile = parentFile;
            }

        }

        cursor.close();


    }

    /**
     * 获取最近的100张照片
     *
     * @param maxCount
     */
    private void getLatestImagePaths(final int maxCount) {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //判断外部存储卡是否存在
            Toast.makeText(this, "外部存储卡不可用", Toast.LENGTH_LONG).show();
            return;
        }

        //获取URI
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = PhotoWallActivity.this.getContentResolver();

        Cursor cursor = contentResolver.query(uri, null,//查询URI，所有列
                MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ? ", //过滤条件 只查jpeg,jpg,png
                new String[]{"image/jpeg", "image/png", "image/jpg"}, //条件
                MediaStore.Images.Media.DATE_MODIFIED);//排序修改时间


        if (cursor != null) {

            //从最新的开始读取
            if (cursor.moveToLast()) {


                while (cursor.moveToPrevious()) {


                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    MLatestImagePaths.add(path);

                    if (MLatestImagePaths.size() >= maxCount) {
                        break;
                    }


                }

            }
        }
        cursor.close();


    }

    private void initListener() {

    }


    @Override
    public void onItemClick(int position, View itemView) {
        List<String> selectedList = mPhotoAdapter.getSelectedList();


        boolean isLastUse = mPhotoAdapter.getIsLastUse();
        String  path;
        if (isLastUse) {
            path = MLatestImagePaths.get(position);
        } else {
            path =  mCurrentFile.getAbsolutePath()+"/"+mPhotoLists.get(position);
        }


        if (selectedList.contains(path)) {
            selectedList.remove(path);
        } else {
            if(selectedList.size()>=maxNum){

                Toast.makeText(PhotoWallActivity.this, "最大选择"+maxNum+"张", Toast.LENGTH_SHORT).show();
            }else {
                selectedList.add(path);
            }

        }
        mTvTitleRight.setText("确定 ("+selectedList.size()+"/"+maxNum+")");
        mPhotoAdapter.notifyDataSetChanged();

        Log.d("vivi", "onItemClick: " + selectedList.size() + "..........." + path);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }


    @OnClick({R.id.ivTitleLeftClose, R.id.tvTitleRight, R.id.rlBottom})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivTitleLeftClose:
                finish();
                break;
            case R.id.tvTitleRight:
                Toast.makeText(PhotoWallActivity.this, "右边", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rlBottom:

                showPopuWindow();
                break;
        }
    }

    private void showPopuWindow() {


        mPopuListImageWindow = new PopuListImageWindow(this, mPhotoFolders);

        mPopuListImageWindow.setOnPopuWindowItemClickListen(this);
        mPopuListImageWindow.showAsDropDown(mRlBottom);
        lightOff();

        mPopuListImageWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //屏幕变亮
                lightOn();
            }
        });

    }

    private void lightOff() {

        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.alpha = 0.3f;

        getWindow().setAttributes(lp);


    }

    private void lightOn() {

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    @Override
    public void onPopuItemClik(int position, View v) {
        PhotoFolder photoFolder = mPhotoFolders.get(position);


        if (photoFolder.getName().equals(LAST_USER_PHOTO)) {

            mPhotoAdapter.setDatas(MLatestImagePaths);
            mPhotoAdapter.setLastUse(true);
            mPhotoAdapter.setDirPath("");
            mTvPathName.setText(LAST_USER_PHOTO);
            mTvPhotoNum.setText(MLatestImagePaths.size() + "");
        } else {
            mCurrentFile = new File(photoFolder.getPath());

            mPhotoAdapter.setDirPath(mCurrentFile.getAbsolutePath());

            String[] list = mCurrentFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            mPhotoLists = Arrays.asList(list);
            mPhotoAdapter.setDatas(mPhotoLists);

            mPhotoAdapter.setLastUse(false);


            mTvPathName.setText(photoFolder.getName());
            //文件夹图片数量
            mTvPhotoNum.setText(mPhotoLists.size() + "");

        }
        mPhotoAdapter.notifyDataSetChanged();

        mPopuListImageWindow.dismiss();

    }
}
