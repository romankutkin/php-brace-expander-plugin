package com.romankutkin.phpbraceexpander

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.php.lang.PhpFileType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class FileIndentResolverTest {
    private val projectFixture = projectFixture()

    @Test
    fun `resolves two spaces`() = runBlocking {
        // Arrange
        val sut = FileIndentResolver()

        // Act
        val result = withTemporaryIndentOptions(
            useTabCharacter = false,
            indentSize = 2,
        ) {
            val file = readAction {
                createFile()
            }

            readAction {
                sut.resolve(file)
            }
        }

        // Assert
        assertEquals("  ", result)
    }

    @Test
    fun `resolves four spaces`() = runBlocking {
        // Arrange
        val sut = FileIndentResolver()

        // Act
        val result = withTemporaryIndentOptions(
            useTabCharacter = false,
            indentSize = 4,
        ) {
            val file = readAction {
                createFile()
            }

            readAction {
                sut.resolve(file)
            }
        }

        // Assert
        assertEquals("    ", result)
    }

    @Test
    fun `resolves tab when tabs are enabled`() = runBlocking {
        // Arrange
        val sut = FileIndentResolver()

        // Act
        val result = withTemporaryIndentOptions(
            useTabCharacter = true,
        ) {
            val file = readAction {
                createFile()
            }

            readAction {
                sut.resolve(file)
            }
        }

        // Assert
        assertEquals("\t", result)
    }

    /**
     * Runs [action] with temporary PHP indentation settings.
     *
     * The original project settings are restored after [action] completes,
     * even if it throws an exception. This allows tests to verify indentation
     * behavior without affecting other tests.
     */
    private suspend fun <T> withTemporaryIndentOptions(
        useTabCharacter: Boolean,
        indentSize: Int = 4,
        action: suspend () -> T,
    ): T {
        val project = projectFixture.get()
        val settings = CodeStyle.createTestSettings(
            CodeStyle.getSettings(project)
        )

        settings.getIndentOptions(PhpFileType.INSTANCE).apply {
            INDENT_SIZE = indentSize
            USE_TAB_CHARACTER = useTabCharacter
        }

        CodeStyle.setTemporarySettings(project, settings)

        try {
            return action()
        } finally {
            CodeStyle.dropTemporarySettings(project)
        }
    }

    private fun createFile(): PsiFile {
        val file = PsiFileFactory
            .getInstance(projectFixture.get())
            .createFileFromText(
                "test.php",
                PhpFileType.INSTANCE,
                "<?php"
            )

        return file
    }
}
