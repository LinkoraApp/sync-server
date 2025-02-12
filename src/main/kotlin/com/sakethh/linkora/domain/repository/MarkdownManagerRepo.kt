package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.PlaceHolder
import com.sakethh.linkora.domain.PlaceHolderValue

interface MarkdownManagerRepo {
    fun getRawHtmlBasedOnMDFile(fileLocation: String, placeHolder: Pair<PlaceHolder, PlaceHolderValue>): String
    fun getRawHtmlBasedOnRawMD(rawMD:String): String
}