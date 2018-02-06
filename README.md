### 一、首先看下效果
![tour_view.gif](http://upload-images.jianshu.io/upload_images/5994029-3b9fa0c95ea49abe.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


### 二、分析
发布一个新版本新功能上线之前总是有引导用户操作的覆盖层，常见的是某些按钮高亮，然后一个提示语或者一张漫画图片。
之前遇到这种功能，一直是UI直接做一张全屏的图片，然后我放到ImageView里，功能能实现，但是经常遇到适配问题，或者图片太大OOM。
今天试着搜索了一下看有没有优雅的方式，百度关键字TourView GuideView都能找到demo，但是看源码很乱，或者就是功能太多，我只要一个简单的高亮就行，试着造一个轮子。
    
### 三、代码
    
我们把每一个引导看成一个对象 TourModel，建立 model 的模型：
    
```
public class TourModel {
    private Bitmap bitmap;
    private int type;

    private int bitmapLeft;
    private int bitmapTop;

    private int holeLeft;
    private int holeTop;
    private int holeRight;
    private int holeBottom;

    ...  忽略setter  getter  ...

    /**
     * 设置高亮洞的rect坐标
     * @param holeLeft 洞左
     * @param holeTop 洞上
     * @param holeRight 洞右
     * @param holeBottom 洞下
     */
    public void setHole(int holeLeft, int holeTop, int holeRight, int holeBottom) {
        this.holeLeft = holeLeft;
        this.holeTop = holeTop;
        this.holeRight = holeRight;
        this.holeBottom = holeBottom;
    }

    /**
     * 设置引导图片的位置
     * @param bitmapLeft 图片居左
     * @param bitmapTop 图片居上
     */
    public void setBitmapLocation(int bitmapLeft, int bitmapTop) {
        this.bitmapLeft = bitmapLeft;
        this.bitmapTop = bitmapTop;
    }
}
```
bitmap也就是展示的图片，这个地方有点局限，最好每次只展示一张图片，但是够用，我基本每一个引导只会要求展示一张图片；这个图片的居左和居上需要记录，绘制的时候能用到；然后是高亮区域的坐标，几乎就是点击的一个 view 的上下左右。
    
再来看看自定义 View 做了哪些事：
```
    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        tourModels = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        blurMaskFilter = new BlurMaskFilter(blurWidth, BlurMaskFilter.Blur.OUTER);
        disableHardwareRendering(this);
    }

    /**
     * 禁止硬件加速
     * @param v
     */
    public static void disableHardwareRendering(View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
```
一些初始化，基操，然后关键是这个禁止硬件加速，这个是高亮区域周围的虚化，如果没有这句话，虚化不会显示，这个我在自定义涂鸦的时候搜到的，想不到这里用上了。
再往下走：
```
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
```
测量，这里没有要写的，基本都是全屏。
往下，关键代码 onDraw() ：
```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (position != -1) {
            TourModel tourModel = tourModels.get(position);

            //画高亮区域
            mPaint.setMaskFilter(null);
            //离屏缓存
            int saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);
            canvas.drawColor(Color.parseColor("#cc000000"));
            drawHole(canvas, tourModel);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
            drawHole(canvas, tourModel);
            mPaint.setXfermode(null);
            //恢复画布
            canvas.restoreToCount(saved);

            //画引导的信息
            canvas.drawBitmap(tourModel.getBitmap(), tourModel.getBitmapLeft(), tourModel.getBitmapTop(), mPaint);

            //虚化
            if (blurWidth > 0) {
                mPaint.setMaskFilter(blurMaskFilter);
                drawHole(canvas, tourModel);
            }
        }
    }
```
一个重要的方法 mPaint.setXfermode()，这个方法一共有16种模式，设置这个模式需要两张画布结合才会显示效果，所以这里用到一个我不知道的技术，叫离屏缓存，也就是新生成一张画布，然后在上面绘制内容，最后再恢复画布将内容绘制到view上来。
我们这里用的是 XOR 模式，也就是两张画布重合的地方被舍弃，于是就有了镂空的圆形高亮的孔洞。
恢复画布之后我们再在view上绘制我们的引导图片和虚化效果。
绘制高亮区域 drawHole() 的代码：
```
    /**
     * 画高亮区域
     *
     * @param canvas
     * @param tourModel
     */
    private void drawHole(Canvas canvas, TourModel tourModel) {
        switch (tourModel.getType()) {
            case Type.TYPE_CIRCLE:
                int holeWidth = tourModel.getHoleRight() - tourModel.getHoleLeft();
                int holeHeight = tourModel.getHoleBottom() - tourModel.getHoleTop();
                int radius = holeWidth > holeHeight ? holeWidth / 2 : holeHeight / 2;
                canvas.drawCircle((tourModel.getHoleLeft() + tourModel.getHoleRight()) / 2, (tourModel.getHoleTop() + tourModel.getHoleBottom()) / 2, radius, mPaint);
                break;
            case Type.TYPE_RECT:
                canvas.drawRect(new RectF(tourModel.getHoleLeft(), tourModel.getHoleTop(), tourModel.getHoleRight(), tourModel.getHoleBottom()), mPaint);
                break;
            case Type.TYPE_OVAL:
            default:
                canvas.drawOval(new RectF(tourModel.getHoleLeft(), tourModel.getHoleTop(), tourModel.getHoleRight(), tourModel.getHoleBottom()), mPaint);
                break;
        }
    }
```
判断类型，三种，分别是圆形、矩形和椭圆，这里矩形没有加圆角，可以扩展。
基本就这些，代码我已经做成库了，可以一键导入：
```
    compile 'com.rjp:TourView:1.0.0'
```
用法如下，xml 里加入或者直接 addView 到 activity 的根布局：
```
    <com.rjp.aopdemo.tour.TourView
            android:id="@+id/tour_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            />
```
我这里为了方便直接加入xml了，然后在你点击某一个 view 的时候：
```
    TourModel tourModel = new TourModel();
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg_float);
    tourModel.setBitmap(bitmap);
    tourModel.setBitmapLocation(v.getLeft(), v.getBottom() + 80);
    tourModel.setHole(v.getLeft() - 20, v.getTop() - 10, v.getRight() + 20, v.getBottom() + 10);
    tourModel.setType(TYPE_CIRCLE);
    tourView.add(tourModel);
    tourView.next();
```
新建一个 model，设置 bitmap 以及 bitmap 的位置，设置高亮区域的坐标，设置高亮区域类型，然后 add 进 TourView，调用 next() 方法开始引导。
    
非常方便，有用给个star。不算原创，看的几个demo做的总结。[GitHub地址](https://github.com/rjpacket/TourView)
      