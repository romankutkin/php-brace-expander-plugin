package com.romankutkin.phpbraceexpander

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.PhpClass

/**
 * Moves a class-like opening brace to its own line before PhpStorm handles Enter.
 * PhpStorm then creates the body line, applies indentation, and positions the caret.
 */
class PhpBraceExpanderEnterHandler : EnterHandlerDelegateAdapter() {
    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffsetRef: Ref<Int>,
        caretAdvanceRef: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?,
    ): EnterHandlerDelegate.Result {
        if (file !is PhpFile) {
            return EnterHandlerDelegate.Result.Continue
        }

        val document = editor.document
        val text = document.charsSequence
        val caretOffset = caretOffsetRef.get()

        if (!isCaretBetweenBraces(text, caretOffset)) {
            return EnterHandlerDelegate.Result.Continue
        }

        if (!isEmptyNamedClassBody(file, caretOffset)) {
            return EnterHandlerDelegate.Result.Continue
        }

        val openingBraceOffset = caretOffset - 1
        if (isOpeningBraceOnSeparateLine(document, openingBraceOffset)) {
            return EnterHandlerDelegate.Result.Continue
        }

        caretOffsetRef.set(moveOpeningBraceToNextLine(document, caretOffset))

        return EnterHandlerDelegate.Result.Continue
    }

    private fun isCaretBetweenBraces(text: CharSequence, caretOffset: Int): Boolean {
        if (caretOffset <= 0 || caretOffset >= text.length) {
            return false
        }

        return text[caretOffset - 1] == OPENING_BRACE && text[caretOffset] == CLOSING_BRACE
    }

    private fun isEmptyNamedClassBody(file: PhpFile, caretOffset: Int): Boolean {
        val openingBrace = file.findElementAt(caretOffset - 1) ?: return false
        val classDeclaration = PsiTreeUtil.getParentOfType(openingBrace, PhpClass::class.java, false)
            ?: return false

        return !classDeclaration.isAnonymous && classDeclaration.textRange.endOffset == caretOffset + 1
    }

    private fun isOpeningBraceOnSeparateLine(document: Document, openingBraceOffset: Int): Boolean {
        val lineStartOffset = document.getLineStartOffset(document.getLineNumber(openingBraceOffset))
        val text = document.charsSequence

        return findFirstNonWhitespaceOffset(text, lineStartOffset, openingBraceOffset) == openingBraceOffset
    }

    private fun moveOpeningBraceToNextLine(document: Document, caretOffset: Int): Int {
        val text = document.charsSequence
        val openingBraceOffset = caretOffset - 1
        val lineStartOffset = document.getLineStartOffset(document.getLineNumber(openingBraceOffset))
        val spacesBeforeBraceOffset = findStartOfWhitespaceBeforeBrace(text, lineStartOffset, openingBraceOffset)
        val indentEndOffset = findFirstNonWhitespaceOffset(text, lineStartOffset, openingBraceOffset)
        val lineIndent = text.subSequence(lineStartOffset, indentEndOffset)
        val lineBreakWithIndent = "\n$lineIndent"

        document.replaceString(spacesBeforeBraceOffset, openingBraceOffset, lineBreakWithIndent)

        val removedCharacters = openingBraceOffset - spacesBeforeBraceOffset
        return caretOffset + lineBreakWithIndent.length - removedCharacters
    }

    private fun findStartOfWhitespaceBeforeBrace(
        text: CharSequence,
        lineStartOffset: Int,
        openingBraceOffset: Int,
    ): Int {
        var offset = openingBraceOffset
        while (offset > lineStartOffset && isIndentCharacter(text[offset - 1])) {
            offset--
        }
        return offset
    }

    private fun findFirstNonWhitespaceOffset(text: CharSequence, startOffset: Int, endOffset: Int): Int {
        var offset = startOffset
        while (offset < endOffset && isIndentCharacter(text[offset])) {
            offset++
        }
        return offset
    }

    private fun isIndentCharacter(character: Char): Boolean {
        return character == ' ' || character == '\t'
    }

    private companion object {
        const val OPENING_BRACE = '{'
        const val CLOSING_BRACE = '}'
    }
}
