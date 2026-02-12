package moe.matsuri.nb4a.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.TypedArrayUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.setPadding
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.getColorAttr
import io.nekohasekai.sagernet.utils.Theme
import kotlin.math.roundToInt

class ColorPickerPreference
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = TypedArrayUtils.getAttr(
        context,
        androidx.preference.R.attr.editTextPreferenceStyle,
        android.R.attr.editTextPreferenceStyle
    )
) : Preference(
    context, attrs, defStyle
) {

    var inited = false

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val widgetFrame = holder.findViewById(android.R.id.widget_frame) as LinearLayout

        if (!inited) {
            inited = true

            widgetFrame.addView(
                getNekoImageViewAtColor(
                    context.getColorAttr(R.attr.colorPrimary),
                    48,
                    0
                )
            )
            widgetFrame.visibility = View.VISIBLE
        }
    }

    fun getNekoImageViewAtColor(color: Int, sizeDp: Int, paddingDp: Int): ImageView {
        // dp to pixel
        val factor = context.resources.displayMetrics.density
        val size = (sizeDp * factor).roundToInt()
        val paddingSize = (paddingDp * factor).roundToInt()

        return ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(size, size)
            setPadding(paddingSize)
            setImageDrawable(getNekoAtColor(resources, color))
        }
    }

    fun getNekoAtColor(res: Resources, color: Int): Drawable {
        val neko = ResourcesCompat.getDrawable(
            res,
            R.drawable.ic_baseline_fiber_manual_record_24,
            null
        )!!
        DrawableCompat.setTint(neko.mutate(), color)
        return neko
    }

    override fun onClick() {
        super.onClick()

        lateinit var dialog: AlertDialog

        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Add Dynamic Colors option if available (Android 12+)
        if (DynamicColors.isDynamicColorAvailable()) {
            val dynamicOption = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(32, 24, 32, 24)
                setOnClickListener {
                    persistInt(Theme.DYNAMIC)
                    dialog.dismiss()
                    callChangeListener(Theme.DYNAMIC)
                }

                // Dynamic colors icon (using system accent color)
                val dynamicColor = context.getColorAttr(com.google.android.material.R.attr.colorPrimary)
                addView(getNekoImageViewAtColor(dynamicColor, 48, 0))

                // Label
                addView(TextView(context).apply {
                    text = context.getString(R.string.theme_dynamic)
                    textSize = 16f
                    setPadding(16, 0, 0, 0)
                })
            }
            mainLayout.addView(dynamicOption)

            // Divider
            mainLayout.addView(View(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1
                )
                setBackgroundColor(context.getColorAttr(com.google.android.material.R.attr.colorOutline))
            })
        }

        val grid = GridLayout(context).apply {
            columnCount = 4

            val colors = context.resources.getIntArray(R.array.material_colors)
            var i = 0

            for (color in colors) {
                i++ //Theme.kt

                val themeId = i
                val view = getNekoImageViewAtColor(color, 64, 0).apply {
                    setOnClickListener {
                        persistInt(themeId)
                        dialog.dismiss()
                        callChangeListener(themeId)
                    }
                }
                addView(view)
            }

        }

        mainLayout.addView(grid)

        dialog = MaterialAlertDialogBuilder(context).setTitle(title)
            .setView(mainLayout)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
