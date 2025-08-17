package ru.netology.nmedia.util

import android.os.Bundle

var Bundle.textArg: String?
    get() = StringArg.getValue(this, this::textArg)
    set(value) = StringArg.setValue(this, this::textArg, value)

var Bundle.postIdArg: Long
    get() = LongArg.getValue(this, this::postIdArg)
    set(value) = LongArg.setValue(this, this::postIdArg, value)