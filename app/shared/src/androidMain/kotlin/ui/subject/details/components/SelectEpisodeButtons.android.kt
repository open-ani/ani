/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.details.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.collection.progress.TestSubjectProgressInfos
import me.him188.ani.app.ui.subject.collection.progress.rememberTestSubjectProgressState
import me.him188.ani.utils.platform.annotations.TestOnly


@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsDone() {
    ProvideFoundationCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.Done,
                ),
                {},
            )
        }
    }
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsContinue() {
    ProvideFoundationCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.ContinueWatching2,
                ),
                {},
            )
        }
    }
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsWatched() {
    ProvideFoundationCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.Watched2,
                ),
                {},
            )
        }
    }
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
private fun PreviewSelectEpisodeButtonsNotOnAir() {
    ProvideFoundationCompositionLocalsForPreview {
        Surface {
            SubjectDetailsDefaults.SelectEpisodeButtons(
                rememberTestSubjectProgressState(
                    TestSubjectProgressInfos.NotOnAir,
                ),
                {},
            )
        }
    }
}
