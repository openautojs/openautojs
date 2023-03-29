package com.stardust.autojs.runtime.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.baidu.paddle.lite.demo.ocr.OcrResult
import com.baidu.paddle.lite.demo.ocr.Predictor
import com.stardust.app.GlobalAppContext
import com.stardust.autojs.core.image.ImageWrapper

class Paddle {
    private val mPredictor: Predictor = Predictor()
    fun initOcr(context: Context?): Boolean {
        return mPredictor.init(context)
    }

    fun initOcr(context: Context?, cpuThreadNum: Int): Boolean {
        return mPredictor.init(context, cpuThreadNum)
    }

    fun initOcr(
        context: Context?,
        cpuThreadNum: Int,
        useSlim: Boolean?,
        useOpencl: Boolean?
    ): Boolean {
        return mPredictor.init(context, cpuThreadNum, useSlim, useOpencl)
    }

    fun initOcr(
        context: Context?,
        cpuThreadNum: Int,
        myModelPath: String?,
        useOpencl: Boolean?
    ): Boolean {
        return mPredictor.init(context, cpuThreadNum, myModelPath, useOpencl)
    }

    fun ocr(
        image: ImageWrapper?, cpuThreadNum: Int, myModelPath: String?, useOpencl: Boolean?,
        detLongSize: Int, scoreThreshold: Float
    ): List<OcrResult> {
        if (image == null) {
            return emptyList<OcrResult>()
        }
        val bitmap: Bitmap = image.getBitmap()
        if (bitmap == null || bitmap.isRecycled()) {
            return emptyList<OcrResult>()
        }
        if (!mPredictor.isLoaded()) {
            mPredictor.init(
                GlobalAppContext.get(),
                cpuThreadNum,
                myModelPath,
                useOpencl,
                detLongSize,
                scoreThreshold
            )
        }
        return mPredictor.ocr(bitmap)
    }

    fun ocr(
        image: ImageWrapper?, cpuThreadNum: Int, useSlim: Boolean?, useOpencl: Boolean?,
        detLongSize: Int, scoreThreshold: Float
    ): List<OcrResult> {
        if (image == null) {
            return emptyList<OcrResult>()
        }
        val bitmap: Bitmap = image.getBitmap()
        if (bitmap == null || bitmap.isRecycled()) {
            return emptyList<OcrResult>()
        }
        if (!mPredictor.isLoaded()) {
            mPredictor.init(
                GlobalAppContext.get(),
                cpuThreadNum,
                useSlim,
                useOpencl,
                detLongSize,
                scoreThreshold
            )
        }
        return mPredictor.ocr(bitmap)
    }

    fun ocr(
        image: ImageWrapper?,
        cpuThreadNum: Int,
        useSlim: Boolean?,
        useOpencl: Boolean?
    ): List<OcrResult> {
        if (image == null) {
            return emptyList<OcrResult>()
        }
        val bitmap: Bitmap = image.getBitmap()
        if (bitmap == null || bitmap.isRecycled()) {
            return emptyList<OcrResult>()
        }
        if (!mPredictor.isLoaded()) {
            mPredictor.init(GlobalAppContext.get(), cpuThreadNum, useSlim, useOpencl)
        }
        return mPredictor.ocr(bitmap)
    }

    fun ocr(
        image: ImageWrapper?,
        cpuThreadNum: Int,
        myModelPath: String?,
        useOpencl: Boolean?
    ): List<OcrResult> {
        if (image == null) {
            return emptyList<OcrResult>()
        }
        val bitmap: Bitmap = image.getBitmap()
        if (bitmap == null || bitmap.isRecycled()) {
            return emptyList<OcrResult>()
        }
        if (!mPredictor.isLoaded()) {
            mPredictor.init(GlobalAppContext.get(), cpuThreadNum, myModelPath, useOpencl)
        }
        return mPredictor.ocr(bitmap)
    }

    fun ocr(image: ImageWrapper?, cpuThreadNum: Int, myModelPath: String?): List<OcrResult> {
        return ocr(image, cpuThreadNum, myModelPath, false)
    }

    fun ocr(image: ImageWrapper?): List<OcrResult> {
        return ocr(image, 4, true, false)
    }

    fun ocrText(
        image: ImageWrapper?,
        cpuThreadNum: Int,
        useSlim: Boolean?,
        useOpencl: Boolean?
    ): Array<String> {
        if (!mPredictor.isLoaded()) {
            mPredictor.init(GlobalAppContext.get(), cpuThreadNum, useSlim, useOpencl)
        }
        val words_result: List<OcrResult> = ocr(image, cpuThreadNum, useSlim, useOpencl)
        val outputResult: Array<String> = arrayOfNulls(words_result.size)
        for (i in words_result.indices) {
            outputResult[i] = words_result[i].words
            Log.i("outputResult", outputResult[i]) // show LOG in Logcat panel
        }
        return outputResult
    }

    fun ocrText(
        image: ImageWrapper?,
        cpuThreadNum: Int,
        myModelPath: String?,
        useOpencl: Boolean?
    ): Array<String> {
        val words_result: List<OcrResult> = ocr(image, cpuThreadNum, myModelPath, useOpencl)
        val outputResult: Array<String> = arrayOfNulls(words_result.size)
        for (i in words_result.indices) {
            outputResult[i] = words_result[i].words
            Log.i("outputResult", outputResult[i]) // show LOG in Logcat panel
        }
        return outputResult
    }

    fun ocrText(image: ImageWrapper?, cpuThreadNum: Int, useSlim: Boolean?): Array<String> {
        return ocrText(image, cpuThreadNum, useSlim, false)
    }

    fun ocrText(image: ImageWrapper?, cpuThreadNum: Int, myModelPath: String?): Array<String> {
        return ocrText(image, cpuThreadNum, myModelPath, false)
    }

    fun ocrText(image: ImageWrapper?): Array<String> {
        return ocrText(image, 4, true, false)
    }

    fun ocrText(image: ImageWrapper?, cpuThreadNum: Int): Array<String> {
        return ocrText(image, cpuThreadNum, true, false)
    }

    fun releaseOcr() {
        mPredictor.releaseModel()
    }

    fun release() {
        mPredictor.releaseModel()
    }
}
