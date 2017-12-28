package com.lhc.customprogressbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：lhc
 * 时间：2017/12/27.
 */

public class CircleProgressView extends ProgressBar {
    /**
     * 进度条宽度
     */
    private final int progressWidth = 40;
    /**
     * 节点线条宽度
     */
    private final int nodeLineWidth = 5;
    /**
     * 节点圆形半径
     */
    private final int nodeCircleRadius = 8;
    /**
     * 起始角度
     */
    private float startAngle = -200;
    /**
     * 滑动角度
     */
    private float sweepAngle = 220;
    /**
     * 渐变颜色
     */
    private int[] colors = {Color.parseColor("#33b2f2"), Color.parseColor("#3675c6"), Color.parseColor("#365db5")};
    /**
     * 文字大小
     */
    private int nodeTxtSize = 45;
    /**
     * 当前选中的节点
     */
    private int curNodeId;

    private LinearGradient linearGradient;
    private List<Node> nodeList = new ArrayList<>();
    private RectF rectFOut;
    private RectF rectFIn;
    /**
     * 中心点x
     */
    private float centerX;
    /**
     * 中心点y
     */
    private float centerY;
    private float radiusForCircle;
    private float radiusForLineOut;
    private float radiusForLineIn;
    private boolean isInTouch;
    /**
     * 手指当前触摸点的进度
     */
    private float touchProgress;
    /**
     * 节点圆、直线的颜色
     */
    private int nodeColor = Color.parseColor("#565c80");
    /**
     * 节点文字颜色
     */
    private int nodeTextColor = Color.parseColor("#999999");
    /**
     * 进度条标签
     */
    private String progressLabel = "风险收益等级";
    private Paint paint;
    private Paint.FontMetrics fm;
    private Path progressInPath;
    private PathMeasure pathMeasure;
    private Matrix matrix;
    private Bitmap arrowBitmap;
    private Region touchRegion;
    /**
     * 标签字体大小
     */
    private float labelTxtSize = 60;
    /**
     * 当前选择项信息字体大小
     */
    private float idTxtSize = 140;
    private OnProgressChangeListener listener;

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        rectFOut = new RectF();
        rectFIn = new RectF();

        fm = paint.getFontMetrics();

        matrix = new Matrix();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_progress_arrow);
        Matrix matrix = new Matrix();
        matrix.postScale(1.5f, 1.5f);
        arrowBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        touchRegion = new Region();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        float width = w * 2 / 3;
        float height = w * 2 / 3;


        rectFOut.left = w / 2 - width / 2;
        rectFOut.right = w / 2 + width / 2;
        rectFOut.top = h / 2 - height / 2;
        rectFOut.bottom = h / 2 + height / 2;

        rectFIn.set(rectFOut);
        rectFIn.left += progressWidth;
        rectFIn.top += progressWidth;
        rectFIn.right -= progressWidth;
        rectFIn.bottom -= progressWidth;

        progressInPath = new Path();
        progressInPath.addArc(rectFIn, startAngle, sweepAngle);
        pathMeasure = new PathMeasure(progressInPath, false);

        radiusForCircle = rectFIn.width() / 2 - progressWidth / 2;
        radiusForLineIn = rectFIn.width() / 2;
        radiusForLineOut = rectFOut.width() / 2 + progressWidth;

        centerX = rectFOut.centerX();
        centerY = rectFOut.centerY();

        linearGradient = new LinearGradient(rectFOut.left, rectFOut.top, rectFOut.right, rectFOut.bottom, colors, null, Shader.TileMode.CLAMP);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isInTouch = touchRegion.contains((int) event.getX(), (int) event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (isInTouch) {
                    touchProgress = getTouchAngleToProgress((int) event.getX(), (int) event.getY());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isInTouch) {
                    refreshCurId();
                    resetAnim();
                }
                break;
        }
        invalidate();
        return true;
    }

    private void resetAnim() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(touchProgress, nodeList.get(curNodeId).getProgress());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                touchProgress = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isInTouch = false;
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }

    private float getTouchAngleToProgress(int x, int y) {
        float ao;
        float progress;
        if (x < centerX) {
            ao = (float) (Math.atan((centerY - y) / (centerX - x)) / Math.PI * 180) - 180;
            progress = (ao - startAngle) * 1f / sweepAngle;
        } else if (x > centerX) {
            ao = (float) (Math.atan((centerY - y) / (centerX - x)) / Math.PI * 180);
            progress = (ao - startAngle) * 1f / sweepAngle;
        } else {
            progress = 0.5f;
        }
        return progress;
    }

    private void refreshCurId() {
        int i = 1;
        for (; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if (node.getProgress() > touchProgress) {
                Node pre = nodeList.get(i - 1);

                if (pre == null) {
                    curNodeId = i;
                } else {
                    float center = (pre.getProgress() + node.getProgress()) / 2;
                    curNodeId = center >= touchProgress ? i - 1 : i;
                }

                break;
            }
        }

        if (listener != null) {
            listener.onChange(curNodeId);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgressCircle(canvas);
        drawNodes(canvas);
        drawArrow(canvas);
        drawLabel(canvas);
        drawCurId(canvas);
    }

    private void drawCurId(Canvas canvas) {
        paint.setTextSize(idTxtSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float txtX = centerX;
        float txtY = centerY - (fontMetrics.bottom - fontMetrics.top) / 2;
        paint.setColor(nodeColor);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(curNodeId + "级", txtX, txtY, paint);
    }

    private void drawLabel(Canvas canvas) {
        paint.setTextSize(labelTxtSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        paint.setColor(nodeTextColor);//设置文字颜色
        paint.setTextAlign(Paint.Align.CENTER);
        float txtX = centerX;
        float txtY = centerY + (fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(progressLabel, txtX, txtY, paint);
    }

    private void drawArrow(Canvas canvas) {

        if (!isInTouch) {
            if (nodeList.size() == 0)
                return;

            if (curNodeId < 0)
                curNodeId = 0;

            if (curNodeId > nodeList.size())
                curNodeId = nodeList.size() - 1;

            touchProgress = nodeList.get(curNodeId).getProgress();
        }

        drawBitmap(canvas, touchProgress);
        initTouchRegion(canvas);
    }

    private void drawBitmap(Canvas canvas, float progress) {
        matrix.reset();
        pathMeasure.getMatrix(pathMeasure.getLength() * progress, matrix, PathMeasure.POSITION_MATRIX_FLAG | PathMeasure.TANGENT_MATRIX_FLAG);
        matrix.preTranslate(-arrowBitmap.getWidth() / 2, -arrowBitmap.getHeight() * 0.7f);//此处调整bitmap绘制位置，一般调整y坐标即可
        canvas.drawBitmap(arrowBitmap, matrix, null);
    }

    private void initTouchRegion(Canvas canvas) {
        RectF bitmapRectF = new RectF(0, 0, arrowBitmap.getWidth(), arrowBitmap.getHeight());
        matrix.mapRect(bitmapRectF);
        Path path = new Path();
        path.addCircle(bitmapRectF.centerX(), bitmapRectF.centerY(), Math.min(arrowBitmap.getWidth(), arrowBitmap.getHeight()), Path.Direction.CW);

        RectF touchRectF = new RectF();
        path.computeBounds(touchRectF, true);
        touchRegion.setPath(path, new Region((int) touchRectF.left, (int) touchRectF.top, (int) touchRectF.right, (int) touchRectF.bottom));
    }

    private void drawNodes(Canvas canvas) {
        if (nodeList.size() == 0)
            return;

        float average = 100f / (nodeList.size() - 1);//每点进度公式为： 100 / （节点数量 - 2 + 1)  PS:2为首尾节点

        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            float angle = 0;
            float progress = 0;
            if (i == 0) {
                angle = startAngle;
            } else if (i == nodeList.size() - 1) {
                angle = startAngle + sweepAngle;
            } else {
                angle = startAngle + i * average / 100 * sweepAngle;
            }

            progress = (angle - startAngle) * 1f / sweepAngle;

            node.setProgress(progress);//设置所占进度(0-1)

            double radian = (angle) * (Math.PI / 180);
            int xForCircle = (int) (centerX + radiusForCircle * Math.cos(radian));
            int yForCircle = (int) (centerY + radiusForCircle * Math.sin(radian));

            int xLineIn = (int) (centerX + radiusForLineIn * Math.cos(radian));
            int yLineIn = (int) (centerY + radiusForLineIn * Math.sin(radian));
            int xLineOut = (int) (centerX + radiusForLineOut * Math.cos(radian));
            int yLineOut = (int) (centerY + radiusForLineOut * Math.sin(radian));

            float radiusForTxt = radiusForLineOut + progressWidth / 2;//为文字增加间距
            int xForTxt = (int) (centerX + radiusForTxt * Math.cos(radian));
            int yForTxt = (int) (centerY + radiusForTxt * Math.sin(radian));

            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(nodeLineWidth);
            paint.setColor(nodeColor);

            canvas.drawCircle(xForCircle, yForCircle, nodeCircleRadius, paint);//绘制每个节点对应的圆点
            canvas.drawLine(xLineOut, yLineOut, xLineIn, yLineIn, paint);//绘制每个节点对应的线

            paint.setTextSize(nodeTxtSize);
            paint.setColor(nodeTextColor);//设置文字颜色
            if (xLineOut < centerX) {
                paint.setTextAlign(Paint.Align.RIGHT);
            } else if (xLineIn > centerX) {
                paint.setTextAlign(Paint.Align.LEFT);
            } else {
                paint.setTextAlign(Paint.Align.CENTER);
            }
            canvas.drawText(node.info, xForTxt, yForTxt + (fm.bottom - fm.top) / 2 - fm.bottom, paint);
        }
    }

    private void drawProgressCircle(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(progressWidth);
        paint.setShader(linearGradient);
        canvas.drawArc(rectFOut, startAngle, sweepAngle, false, paint);

        paint.setShader(null);
        paint.setColor(nodeColor);
        paint.setStrokeWidth(5);
        canvas.drawPath(progressInPath, paint);
    }

    public void addNodes(List<Node> list) {
        nodeList.clear();
        nodeList.addAll(list);
    }

    public void setLabel(String label) {
        this.progressLabel = label;
    }

    public void addNode(Node node) {
        nodeList.add(node);
    }

    public void draw() {
        invalidate();
    }

    public static class Node {

        public Node() {
        }

        public Node(String info) {
            this.info = info;
        }

        public String info = "";
        private float progress;

        public void setProgress(float progress) {
            this.progress = progress;
        }

        public float getProgress() {
            return progress;
        }
    }

    public void setListener(OnProgressChangeListener listener) {
        this.listener = listener;
    }

    public interface OnProgressChangeListener {
        void onChange(int curId);
    }

}
