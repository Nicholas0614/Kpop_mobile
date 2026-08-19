package com.example.kpop

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AdminApi
import com.example.kpop.network.model.AdminDashboard
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {

    private val adminApi by lazy {
        RetrofitClient.create(this, AdminApi::class.java)
    }

    private lateinit var orderStatusChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminMain)) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            Toast.makeText(
                this,
                "Admin access only",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        orderStatusChart = findViewById(R.id.orderStatusChart)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    moveTaskToBack(true)
                }
            }
        )

        findViewById<Button>(R.id.btnManageProducts).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AdminProductsActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.btnManageOrders).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AdminOrdersActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.btnManageCategories).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AdminCategoriesActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.btnManageGroups).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AdminGroupsActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.btnManageCoupons).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AdminCouponsActivity::class.java)
            )
        }

        findViewById<ImageButton>(R.id.btnAdminLogout).setOnClickListener {

            val view = layoutInflater.inflate(
                R.layout.logout_dialog,
                null
            )

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .create()

            dialog.window?.setBackgroundDrawableResource(
                android.R.color.transparent
            )

            view.findViewById<Button>(
                R.id.btnCancelLogout
            ).setOnClickListener {

                dialog.dismiss()
            }

            view.findViewById<Button>(
                R.id.btnConfirmLogout
            ).setOnClickListener {

                SessionManager(this).logout()

                val intent = Intent(
                    this,
                    MainActivity::class.java
                )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                finish()

                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onResume() {
        super.onResume()

        loadDashboard()
    }

    private fun loadDashboard() {

        adminApi.getDashboard()
            .enqueue(object : Callback<AdminDashboard> {

                override fun onResponse(
                    call: Call<AdminDashboard>,
                    response: Response<AdminDashboard>
                ) {

                    val data = response.body()

                    if (!response.isSuccessful || data == null) {

                        Toast.makeText(
                            this@AdminActivity,
                            response.errorBody()?.string()
                                ?: "Unable to load dashboard",
                            Toast.LENGTH_SHORT
                        ).show()

                        return
                    }

                    findViewById<TextView>(
                        R.id.txtTotalUsers
                    ).text = "Users\n${data.totalUsers}"

                    findViewById<TextView>(
                        R.id.txtTotalProducts
                    ).text = "Products\n${data.totalProducts}"

                    findViewById<TextView>(
                        R.id.txtProductsOnSale
                    ).text = "On Sale\n${data.productsOnSale}"

                    findViewById<TextView>(
                        R.id.txtTotalOrders
                    ).text = "Orders\n${data.totalOrders}"

                    findViewById<TextView>(
                        R.id.txtPaidOrders
                    ).text = "Paid\n${data.paidOrders}"

                    findViewById<TextView>(
                        R.id.txtProcessingOrders
                    ).text = "Processing\n${data.processingOrders}"

                    findViewById<TextView>(
                        R.id.txtShippedOrders
                    ).text = "Shipped\n${data.shippedOrders}"

                    findViewById<TextView>(
                        R.id.txtDeliveredOrders
                    ).text = "Delivered\n${data.deliveredOrders}"

                    findViewById<TextView>(
                        R.id.txtRevenue
                    ).text =
                        "Revenue\nRM%.2f".format(data.totalRevenue)


                    // UPDATE GRAPH
                    setupOrderStatusChart(
                        paid = data.paidOrders.toFloat(),
                        processing = data.processingOrders.toFloat(),
                        shipped = data.shippedOrders.toFloat(),
                        delivered = data.deliveredOrders.toFloat()
                    )
                }

                override fun onFailure(
                    call: Call<AdminDashboard>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        this@AdminActivity,
                        "Server error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun setupOrderStatusChart(
        paid: Float,
        processing: Float,
        shipped: Float,
        delivered: Float
    ) {

        val entries = listOf(
            BarEntry(0f, paid),
            BarEntry(1f, processing),
            BarEntry(2f, shipped),
            BarEntry(3f, delivered)
        )

        val dataSet = BarDataSet(
            entries,
            ""
        )

        // Soft pink/mauve to match #FFF7FB theme
        dataSet.color = Color.parseColor("#C58FA8")

        dataSet.valueTextColor =
            Color.parseColor("#333333")

        dataSet.valueTextSize = 12f

        // Show 1 instead of 1.0
        dataSet.valueFormatter =
            object : ValueFormatter() {

                override fun getFormattedValue(
                    value: Float
                ): String {

                    return value
                        .toInt()
                        .toString()
                }
            }

        val barData = BarData(dataSet)

        barData.barWidth = 0.55f

        orderStatusChart.data = barData


        // GENERAL
        orderStatusChart.description.isEnabled = false

        orderStatusChart.legend.isEnabled = false

        orderStatusChart.setDrawGridBackground(false)

        orderStatusChart.setDrawBarShadow(false)

        orderStatusChart.setFitBars(true)


        // Disable chart moving / zooming
        orderStatusChart.setScaleEnabled(false)

        orderStatusChart.setPinchZoom(false)

        orderStatusChart.setDoubleTapToZoomEnabled(false)


        // RIGHT AXIS
        orderStatusChart.axisRight.isEnabled = false


        // LEFT AXIS
        orderStatusChart.axisLeft.apply {

            axisMinimum = 0f

            granularity = 1f

            textColor =
                Color.parseColor("#8B8286")

            textSize = 10f

            setDrawAxisLine(false)

            setDrawGridLines(true)

            gridColor =
                Color.parseColor("#EEE8EB")
        }


        // BOTTOM AXIS
        orderStatusChart.xAxis.apply {

            valueFormatter =
                IndexAxisValueFormatter(
                    listOf(
                        "Paid",
                        "Processing",
                        "Shipped",
                        "Delivered"
                    )
                )

            position =
                XAxis.XAxisPosition.BOTTOM

            granularity = 1f

            setDrawGridLines(false)

            setDrawAxisLine(false)

            textColor =
                Color.parseColor("#555555")

            textSize = 10f
        }


        // Nice little animation
        orderStatusChart.animateY(700)

        orderStatusChart.invalidate()
    }
}