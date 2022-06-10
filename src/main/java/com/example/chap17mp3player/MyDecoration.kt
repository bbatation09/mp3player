package com.example.chap17mp3player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class MyDecoration(val context: Context) : RecyclerView.ItemDecoration() {
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        //리사이클러뷰 전체사이즈
//        val width = parent.width
//        val height = parent.height
//
//        //이미지 사이즈 구하기
//        val drawable : Drawable? = ResourcesCompat.getDrawable(context.resources, R.drawable.exalbum,null)
//        val drWidth = drawable?.intrinsicWidth
//        val drHeight = drawable?.intrinsicHeight
//
//        //중앙 위치 좌표 (x,y 좌표) 찾기
//        var x = (width/2 - drWidth?.div(2) as Int)
//        var y = (height/2 - drHeight?.div(2) as Int)
//
//        //캔버스에 비트맵으로 그림을 그린다
//        c.drawBitmap(BitmapFactory.decodeResource(context.resources,R.drawable.exalbum),x.toFloat(),y.toFloat(),null)

    }

    //각 이이템 뷰마다 적용시킴
    @SuppressLint("ResourceType")
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val index = parent.getChildAdapterPosition(view)
        if(index%2 == 0){
            outRect.set(10,10,10,50)
        }
        else{
            outRect.set(10,10,10,0)
        }

        view.setBackgroundColor(context.getResources().getColor(R.drawable.gradient_background))
        ViewCompat.setElevation(view,10.0f)
    }
}