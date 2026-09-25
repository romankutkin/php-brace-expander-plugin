package com.romankutkin.phpbraceexpander

import com.intellij.application.options.CodeStyle
import com.intellij.psi.PsiFile

class FileIndentResolver {
    fun resolve(file: PsiFile): String {
        val indentOptions = CodeStyle.getIndentOptions(file)

        if (indentOptions.USE_TAB_CHARACTER) {
            return "\t"
        }

        return " ".repeat(indentOptions.INDENT_SIZE)
    }
}
