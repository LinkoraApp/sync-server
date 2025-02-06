package com.sakethh.linkora.domain

class LWWConflictException : Exception("This row already contains the latest data.")