package com.romankutkin.phpbraceexpander

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class PhpBraceExpanderEnterHandlerTest {
    private val project = projectFixture(openAfterCreation = true)
    private val module = project.moduleFixture("src")
    private val tempDirectory = tempPathFixture()
    private val fixture by codeInsightFixture(project, tempDirectory).dependsOn(module)

    @BeforeEach
    fun registerEnterHandler(@TestDisposable disposable: Disposable) {
        EnterHandlerDelegate.EP_NAME.point.registerExtension(
            PhpBraceExpanderEnterHandler(),
            LoadingOrder.FIRST,
            disposable,
        )
    }

    @Test
    fun `expands interface body`() {
        // Arrange
        configureEditor(
            """
                <?php

                interface Something {<caret>}
            """.trimIndent(),
        )

        // Act
        pressEnter()

        // Assert
        assertEditorContent(
            """
                <?php

                interface Something
                {
                    <caret>
                }
            """.trimIndent(),
        )
    }

    @Test
    fun `expands class body`() {
        // Arrange
        configureEditor(
            """
                <?php
                
                class Something {<caret>}
            """.trimIndent()
        )

        // Act
        pressEnter()

        // Assert
        assertEditorContent(
            """
                <?php
                
                class Something
                {
                    <caret>
                }
            """.trimIndent()
        )
    }

    @Test
    fun `expands class body at current indent`() {
        // Arrange
        configureEditor(
            """
                <?php

                if (true) {
                    class Something {<caret>}
                }
            """.trimIndent(),
        )

        // Act
        pressEnter()

        // Assert
        assertEditorContent(
            """
                <?php

                if (true) {
                    class Something
                    {
                        <caret>
                    }
                }
            """.trimIndent(),
        )
    }

    @Test
    fun `expands trait body`() {
        // Arrange
        configureEditor(
            """
                <?php

                trait Something {<caret>}
            """.trimIndent(),
        )

        // Act
        pressEnter()

        // Assert
        assertEditorContent(
            """
                <?php

                trait Something
                {
                    <caret>
                }
            """.trimIndent(),
        )
    }

    @Test
    fun `expands enum body`() {
        // Arrange
        configureEditor(
            """
                <?php

                enum Something {<caret>}
            """.trimIndent(),
        )

        // Act
        pressEnter()

        // Assert
        assertEditorContent(
            """
                <?php

                enum Something
                {
                    <caret>
                }
            """.trimIndent(),
        )
    }

    @Test
    fun `does not move control structure brace`() {
        // Arrange
        configureEditor(
            """
                <?php

                if (true) {<caret>}
            """.trimIndent(),
        )

        // Act
        pressEnter()

        // Assert
        assertEditorContent(
            """
                <?php

                if (true) {
                    <caret>
                }
            """.trimIndent(),
        )
    }

    @Test
    fun `does not move anonymous class brace`() {
        // Arrange
        configureEditor(
            """
                <?php

                ${'$'}value = new class {<caret>};
            """.trimIndent(),
        )

        // Act
        pressEnter()

        // Assert
        assertEditorContent(
            """
                <?php

                ${'$'}value = new class {
                    <caret>
                };
            """.trimIndent(),
        )
    }

    private fun configureEditor(content: String) {
        fixture.configureByText("example.php", content)
    }

    private fun pressEnter() {
        fixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER)
    }

    private fun assertEditorContent(expected: String) {
        fixture.checkResult(expected)
    }
}
