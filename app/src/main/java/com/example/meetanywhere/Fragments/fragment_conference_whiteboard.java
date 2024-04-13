package com.example.meetanywhere.Fragments;

import com.example.meetanywhere.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Stack;

public class fragment_conference_whiteboard extends Fragment {
    private String screenName = "[FRAGMENT fragment_conference_whiteboard]:";
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    private Context context;
    private Activity activity;
    private View fragmentView;

    private DrawCanvas drawCanvas;
    private FloatingActionButton fbPen;             //펜 모드 버튼
    private FloatingActionButton fbEraser;          //지우개 모드 버튼
    private FloatingActionButton fbUndo;            // undo 버튼
    private FloatingActionButton fbRedo;            // redo 버튼
    private FloatingActionButton fbClear;            // 보드 내용 전체 삭제 버튼
    private ConstraintLayout canvasContainer;       //캔버스 root view

    public fragment_conference_whiteboard(
            Activity activity
    ) {
        this.activity = activity;
        this.context = activity.getBaseContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_conference_whiteboard, container, false);
        whiteBoardInit();
        return fragmentView;
    }

    public void whiteBoardInit() {
        canvasContainer = fragmentView.findViewById(R.id.f_conference_whiteboard_layout);
        fbPen = fragmentView.findViewById(R.id.fb_pen);
        fbEraser = fragmentView.findViewById(R.id.fb_eraser);
        fbUndo = fragmentView.findViewById(R.id.fb_undo);
        fbRedo = fragmentView.findViewById(R.id.fb_redo);
        fbClear = fragmentView.findViewById(R.id.fb_clear);
        drawCanvas = new DrawCanvas(context);
        canvasContainer.addView(drawCanvas);

        // 이벤트 등록
        fbPen.setOnClickListener((v) -> {
            drawCanvas.changeTool(DrawCanvas.MODE_PEN);
        });

        fbEraser.setOnClickListener((v) -> {
            drawCanvas.changeTool(DrawCanvas.MODE_ERASER);
        });

        fbUndo.setOnClickListener((v) -> {
            drawCanvas.undo();
        });

        fbRedo.setOnClickListener((v) -> {
            drawCanvas.redo();
        });

        fbClear.setOnClickListener((v) -> {
            drawCanvas.init();
            drawCanvas.clearBoard();
            drawCanvas.invalidate();
        });
    }

    class Pen {
        public static final int STATE_START = 0;        //펜의 상태(움직임 시작)
        public static final int STATE_MOVE = 1;         //펜의 상태(움직이는 중)
        float x, y;                                     //펜의 좌표
        int moveStatus;                                 //현재 움직임 여부
        int color;                                      //펜 색
        int size;                                       //펜 두께

        public Pen(float x, float y, int moveStatus, int color, int size) {
            this.x = x;
            this.y = y;
            this.moveStatus = moveStatus;
            this.color = color;
            this.size = size;
        }

        public boolean isMove() {
            return moveStatus == STATE_MOVE;
        }
    }

    class DrawCanvas extends View {
        public static final int MODE_PEN = 1;                     //모드 (펜)
        public static final int MODE_ERASER = 0;                  //모드 (지우개)
        final int PEN_SIZE = 3;                                   //펜 사이즈
        final int ERASER_SIZE = 70;                               //지우개 사이즈
        int color;                                                //현재 펜 색상
        int size;                                                 //현재 펜 크기
        Paint paint;                                              //펜
        Bitmap loadDrawImage;                                     //호출된 이전 그림

        ArrayList<Pen> drawHistoryList;                           //그리기 경로가 기록된 리스트
        int historyStartIndex = 0;  // 각 그리기 작업 단위에서의 시작 시점. undo, redo 작업을 위해 필요함
        ArrayList<Pen> currentTask;  // Track the current drawing task
        Stack<String> undoStack;
        Stack<String> redoStack;
        boolean hasPerformedUndo = false;   // 어떤 작업을 하기 바로 직전에 undo 작업을 실행했는지에 대한 여부.

        public DrawCanvas(Context context) {
            super(context);
            init();
        }

        public DrawCanvas(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DrawCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            drawHistoryList = new ArrayList<>();
            currentTask = new ArrayList<>();
            loadDrawImage = null;
            color = Color.BLACK;
            size = PEN_SIZE;
            undoStack = new Stack<>();
            redoStack = new Stack<>();
        }

        private void changeTool(int toolMode) {
            if (toolMode == MODE_PEN) {
                this.color = Color.BLACK;
                size = PEN_SIZE;
            } else {
                this.color = Color.WHITE;
                size = ERASER_SIZE;
            }
            paint.setColor(color);
        }

        public void undo() {
            Log.d(tag_execute, "undo");

            if (!undoStack.isEmpty()) {
                Log.d(tag_check, "undoStack is not empty");
                String indexRange = undoStack.pop();
                Log.d(tag_check, "indexRange : " + indexRange);
                int startIndex = Integer.parseInt(indexRange.split("&")[0]);
                int endIndex = Integer.parseInt(indexRange.split("&")[1]);
//                Log.d(tag_check, "startIndex : " + startIndex);
//                Log.d(tag_check, "endIndex : " + endIndex);

                currentTask = new ArrayList<>(drawHistoryList.subList(0, startIndex));
                Log.d(tag_check, "currentTask : " + currentTask.size());
                redoStack.add(indexRange);
                Log.d(tag_check, "redoStack : " + redoStack);
                hasPerformedUndo = true;

                if (currentTask.size() == 0) {
                    Log.d(tag_check, "currentTask is empty");

                }
                invalidate();  // Redraw without the undone path
            }
        }

        // Add this method to handle redo
        public void redo() {
            Log.d(tag_execute, "redo");
            if (!redoStack.isEmpty()) {
                Log.d(tag_check, "redoStack is not empty");
                String indexRange = redoStack.pop();
                Log.d(tag_check, "indexRange : " + indexRange);
                int startIndex = Integer.parseInt(indexRange.split("&")[0]);
                int endIndex = Integer.parseInt(indexRange.split("&")[1]);
//                Log.d(tag_check, "startIndex : " + startIndex);
//                Log.d(tag_check, "endIndex : " + endIndex);

                ArrayList tempList = new ArrayList(drawHistoryList.subList(startIndex, endIndex));
                Log.d(tag_check, "tempList : " + tempList.size());
                currentTask.addAll(tempList);
                Log.d(tag_check, "currentTask : " + currentTask.size());
                undoStack.add(indexRange);
                Log.d(tag_check, "undoStack : " + undoStack);
                //hasPerformedRedo = true;
                invalidate();  // Redraw with the restored path
            }
        }

        public void clearBoard() {
            Log.d(tag_execute, "clearBoard");
            currentTask.clear();
            drawHistoryList.clear();
            historyStartIndex = 0;
            undoStack.clear();
            redoStack.clear();
            hasPerformedUndo = false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //Log.d(tag_event, "onDraw");
            canvas.drawColor(Color.WHITE);

            if (loadDrawImage != null) {
                //Log.d(tag_execute, "loadDrawImage");
                canvas.drawBitmap(loadDrawImage, 0, 0, null);
            }

            for (int i = 0; i < currentTask.size(); i++) {
                Pen p = currentTask.get(i);

                paint.setColor(p.color);
                paint.setStrokeWidth(p.size);

                if (p.isMove()) {
                    //Log.d(tag_check, "p.isMove");
                    Pen prevP = currentTask.get(i - 1);
                    //Log.d(tag_check, "p : " + p);
                    //Log.d(tag_check, "prevP : " + prevP);
                    canvas.drawLine(prevP.x, prevP.y, p.x, p.y, paint);
                    //Log.d(tag_check, "canvas.drawLine");
                }
            }
            //Log.d(tag_check, "drawLine done.");
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            //Log.d(tag_event, "onTouchEvent");
            int action = e.getAction();
            int state = action == MotionEvent.ACTION_DOWN ? Pen.STATE_START : Pen.STATE_MOVE;

            if (action == MotionEvent.ACTION_DOWN) {
                Log.d(tag_execute, "ACTION_DOWN");
                if (hasPerformedUndo) {
                    Log.d(tag_execute, "hasPerformedUndo true.");
                    drawHistoryList = new ArrayList<>(currentTask);
                    historyStartIndex = currentTask.size();
                    Log.d(tag_execute, "drawHistoryList : " + drawHistoryList.size());
                    Log.d(tag_execute, "historyStartIndex : " + historyStartIndex);
                    Log.d(tag_check, "check currentTask : " + currentTask.size());
                    hasPerformedUndo = false;

                    if (!redoStack.isEmpty()) {
                        redoStack.clear();
                        Log.d(tag_execute, "pop redoStack : " + redoStack);
                    }
                }
            }

            // 이 부분 순서 바꾸지 말 것
            currentTask.add(new Pen(e.getX(), e.getY(), state, color, size));
            drawHistoryList.add(new Pen(e.getX(), e.getY(), state, color, size));
            invalidate();
            //

            if (action == MotionEvent.ACTION_UP) {
                // Add this to prevent recording multiple paths when dragging
                Log.d(tag_check, "ACTION_UP");
                int endIndex = drawHistoryList.size();
                //Log.d(tag_check, "startIndex : " + historyStartIndex);
                //Log.d(tag_check, "endIndex : " + endIndex);
                String indexRange = historyStartIndex + "&" + endIndex;
                //Log.d(tag_check, "indexRange : " + indexRange);
                undoStack.add(indexRange);
                Log.d(tag_check, "check undoStack : " + undoStack);

                historyStartIndex = drawHistoryList.size();
                Log.d(tag_check, "update startIndex : " + historyStartIndex);
                Log.d(tag_check, "check drawHistoryList : " + drawHistoryList.size());
                Log.d(tag_check, "check currentTask : " + currentTask.size());
            }
            return true;
        }
    }
    // Reserved code. do not delete!
}
