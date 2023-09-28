package com.fansan.dex2oat.entity

sealed class SourceType(){
    data object UP:SourceType()
    data object UPCompiled:SourceType()
    data object SPCompiled:SourceType()
    data object SP:SourceType()
}
