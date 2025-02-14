package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.PlaceHolder
import com.sakethh.linkora.domain.PlaceHolderValue
import com.sakethh.linkora.domain.repository.MarkdownManagerRepo
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser


class MarkdownManagerRepoImpl : MarkdownManagerRepo {

    private val markdownFlavour = CommonMarkFlavourDescriptor()
    private val mdParser = MarkdownParser(markdownFlavour)

    override fun getRawHtmlBasedOnMDFile(
        fileLocation: String,
        placeHolder: Pair<PlaceHolder, PlaceHolderValue>
    ): String {
        val file = this::class.java.getResourceAsStream(fileLocation)
        val rawMDText = file.use { it?.bufferedReader()?.readText().toString() }
            .replace(placeHolder.first, placeHolder.second)
        val parseTree = mdParser.buildMarkdownTreeFromString(rawMDText)
        return HtmlGenerator(markdownText = rawMDText, parseTree, markdownFlavour).generateHtml()
    }

    override fun getRawHtmlBasedOnRawMD(rawMD: String): String {
        return HtmlGenerator(
            markdownText = rawMD,
            mdParser.buildMarkdownTreeFromString(rawMD),
            markdownFlavour
        ).generateHtml()
    }
}