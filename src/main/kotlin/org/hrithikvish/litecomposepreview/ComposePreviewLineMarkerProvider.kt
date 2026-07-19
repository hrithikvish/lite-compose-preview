package org.hrithikvish.litecomposepreview

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedFunction

private const val PREVIEW_ANNOTATION_SHORT_NAME = "Preview"

class ComposePreviewLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val function = element.parent as? KtNamedFunction ?: return null
        if (function.nameIdentifier !== element) return null
        if (function.annotationEntries.none { it.shortName?.asString() == PREVIEW_ANNOTATION_SHORT_NAME }) return null

        val navigationHandler = GutterIconNavigationHandler<PsiElement> { _, elt ->
            val fn = elt.parent as? KtNamedFunction ?: return@GutterIconNavigationHandler
            ComposePreviewRenderer.render(project = fn.project, function = fn)
        }

        return LineMarkerInfo(
            /* element = */ element,
            /* range = */ element.textRange,
            /* icon = */ AllIcons.Actions.Execute,
            /* tooltipProvider = */ { "Render Compose preview" },
            /* navHandler = */ navigationHandler,
            /* alignment = */ GutterIconRenderer.Alignment.LEFT
        )
    }
}
