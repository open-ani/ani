package me.him188.ani.app.ui.foundation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


private var _QqRoundedOutline: ImageVector? = null

public val AniIcons.QqRoundedOutline: ImageVector
    get() {
        if (_QqRoundedOutline != null) {
            return _QqRoundedOutline!!
        }
        _QqRoundedOutline = ImageVector.Builder(
            name = "QqRoundedOutline",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f,
        ).apply {
            group {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.EvenOdd,
                ) {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(32f)
                    verticalLineToRelative(32f)
                    horizontalLineToRelative(-32f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(15.9998867f, 0f)
                    lineToRelative(-0.3175962f, 0.00373031f)
                    curveToRelative(-6.8341f, 0.1611f, -10.598f, 5.522f, -10.6233f, 13.0996f)
                    lineToRelative(0.00099159f, 0.2727027f)
                    lineToRelative(-0.86256398f, 2.1053645f)
                    curveToRelative(-0.3434f, 0.846f, -0.5948f, 1.4919f, -0.8237f, 2.125f)
                    curveToRelative(-0.1625f, 0.4495f, -0.3109f, 0.8862f, -0.4516f, 1.3303f)
                    curveToRelative(-1.4104f, 4.4529f, -1.47f, 7.7314f, 1.1498f, 8.0425f)
                    lineToRelative(0.19586827f, 0.0182568f)
                    curveToRelative(0.6985f, 0.0463f, 1.1878f, -0.1358f, 1.6935f, -0.5696f)
                    lineToRelative(0.04666502f, -0.0427837f)
                    lineToRelative(0.10703497f, 0.1859114f)
                    lineToRelative(0.16295747f, 0.2641667f)
                    lineToRelative(0.09000756f, 0.1369219f)
                    lineToRelative(-0.10203891f, 0.0725127f)
                    curveToRelative(-1.1846f, 0.86f, -1.7852f, 2.0423f, -0.875f, 3.5478f)
                    curveToRelative(0.7201f, 1.192f, 1.6858f, 1.3852f, 4.6765f, 1.4059f)
                    horizontalLineToRelative(1.2781117f)
                    lineToRelative(0.7210113f, -0.0101194f)
                    curveToRelative(1.4579f, -0.0287f, 2.9764f, -0.1066f, 3.9344f, -0.2001f)
                    lineToRelative(0.2555297f, 0.0243167f)
                    curveToRelative(1.1291f, 0.097f, 2.8606f, 0.1735f, 4.3994f, 0.1859f)
                    horizontalLineToRelative(1.2781118f)
                    curveToRelative(2.9908f, -0.0207f, 3.9564f, -0.214f, 4.6768f, -1.4064f)
                    lineToRelative(0.0983227f, -0.1723424f)
                    curveToRelative(0.7644f, -1.4242f, 0.1648f, -2.5487f, -0.9747f, -3.3755f)
                    lineToRelative(-0.1034569f, -0.072941f)
                    lineToRelative(0.0912293f, -0.1362605f)
                    lineToRelative(0.1629318f, -0.2641511f)
                    lineToRelative(0.1068389f, -0.1855884f)
                    lineToRelative(0.0466679f, 0.0427821f)
                    curveToRelative(0.5517f, 0.4732f, 1.0836f, 0.6469f, 1.889f, 0.5514f)
                    curveToRelative(2.62f, -0.3116f, 2.5605f, -3.5895f, 1.1502f, -8.0425f)
                    curveToRelative(-0.1367f, -0.4315f, -0.281f, -0.8568f, -0.4386f, -1.294f)
                    lineToRelative(-0.1781698f, -0.4846461f)
                    curveToRelative(-0.0923f, -0.2465f, -0.1903f, -0.5013f, -0.2977f, -0.7745f)
                    lineToRelative(-0.3954267f, -0.9868956f)
                    lineToRelative(-0.8290275f, -2.020683f)
                    lineToRelative(0.0009399f, -0.279262f)
                    curveToRelative(-0.0388f, -7.6058f, -3.8169f, -12.9305f, -10.6188f, -13.0929f)
                    close()
                    moveToRelative(0.0001023f, 2f)
                    curveToRelative(6.34f, 0.0003f, 9.0947f, 5.2573f, 8.9335f, 11.7558f)
                    lineToRelative(0.8516442f, 2.0748544f)
                    curveToRelative(0.5607f, 1.3709f, 0.994f, 2.4719f, 1.3861f, 3.7099f)
                    curveToRelative(1.2134f, 3.8314f, 0.8203f, 5.417f, 0.521f, 5.4526f)
                    curveToRelative(-0.6425f, 0.0762f, -2.5006f, -2.8843f, -2.5006f, -2.8843f)
                    curveToRelative(0f, 1.7142f, -0.8995f, 3.951f, -2.8457f, 5.5664f)
                    lineToRelative(0.3377045f, 0.1073023f)
                    curveToRelative(1.0249f, 0.3412f, 2.6654f, 1.0303f, 2.2155f, 1.7751f)
                    curveToRelative(-0.4078f, 0.675f, -6.9962f, 0.431f, -8.8983f, 0.2208f)
                    lineToRelative(-0.4753503f, 0.0452169f)
                    curveToRelative(-2.3364f, 0.1952f, -8.0442f, 0.3608f, -8.4229f, -0.266f)
                    curveToRelative(-0.5042f, -0.834f, 1.612f, -1.5976f, 2.5521f, -1.8821f)
                    curveToRelative(-1.9465f, -1.6154f, -2.8462f, -3.8525f, -2.8462f, -5.5667f)
                    lineToRelative(-0.32111727f, 0.4924567f)
                    curveToRelative(-0.5606f, 0.8385f, -1.7036f, 2.4482f, -2.1794f, 2.3918f)
                    curveToRelative(-0.2993f, -0.0355f, -0.6925f, -1.6212f, 0.521f, -5.4526f)
                    lineToRelative(0.21887662f, -0.6654718f)
                    curveToRelative(0.5222f, -1.5311f, 1.1336f, -2.9516f, 2.0188f, -5.1193f)
                    curveToRelative(-0.1703f, -6.3929f, 2.522f, -11.7554f, 8.9334f, -11.7557f)
                    close()
                }
            }
        }.build()
        return _QqRoundedOutline!!
    }

