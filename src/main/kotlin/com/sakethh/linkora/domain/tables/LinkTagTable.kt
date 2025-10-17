package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object LinkTagTable : Table(name = "link_tags") {
    val lastModified = long("last_modified")
    val linkId = long("linkId").references(LinksTable.id, onDelete = ReferenceOption.CASCADE)
    val tagId = long("tagId").references(TagsTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(linkId, tagId)
}