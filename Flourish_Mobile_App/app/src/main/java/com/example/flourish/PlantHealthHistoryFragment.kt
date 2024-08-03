package com.example.flourish

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYGraphWidget
import com.example.flourish.databinding.FragmentPlantHealthHistoryBinding
import com.example.flourish.db.Repository
import com.example.flourish.helper.DateTimeHandler
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import io.realm.kotlin.types.RealmList
import org.mongodb.kbson.ObjectId
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import kotlin.math.roundToInt

// Fragment to display the health history of a plant
// The health parameters are retrieved from the database and displayed in a plot (using AndroidPlot library)
// The plot shows the health parameters (nitrogen, phosphorus, potassium, pH, temperature, humidity, and light level) over time
class PlantHealthHistoryFragment : Fragment() {

    private var _binding: FragmentPlantHealthHistoryBinding? = null
    private val binding: FragmentPlantHealthHistoryBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var plantId: ObjectId
    private lateinit var plantRepository: Repository<Plant>
    private lateinit var plantParams: RealmList<HealthParameters>
    private lateinit var plant: Plant
    private val seriesNitrogen: MutableList<Number> = mutableListOf()
    private val seriesPhosphorus: MutableList<Number> = mutableListOf()
    private val seriesPotassium: MutableList<Number> = mutableListOf()
    private val seriesPh: MutableList<Number> = mutableListOf()
    private val seriesTemperature: MutableList<Number> = mutableListOf()
    private val seriesHumidity: MutableList<Number> = mutableListOf()
    private val seriesLight: MutableList<Number> = mutableListOf()
    private val datesLabels: MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get MainViewModel and Repository instances
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        plantRepository = Repository(Plant::class)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind layout for this fragment and return the view
        _binding = FragmentPlantHealthHistoryBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set back button click listener to navigate to the ManagePlantFragment
        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ManagePlantFragment())
                commit()
            }
        }

        // Get a plant id from the MainViewModel
        plantId = ObjectId(mainViewModel.plantId)

        try {
            // Get a plant from the database
            plant = plantRepository.getById(plantId)!!
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Plant not found", Toast.LENGTH_SHORT).show()
            Log.e("PlantHealthHistoryFragment", "Plant retrieving error", e)
        }

        // Check if the plant has health parameters
        if (plant.healthParameters.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Not enough data for plant history",
                Toast.LENGTH_SHORT
            ).show()
            Log.i("PlantHealthHistoryFragment", "Not enough data for plant history")
        } else {
            // Get health parameters of the plant
            plantParams = plant.healthParameters
            // Set the plot visibility to visible
            binding.plot.visibility = View.VISIBLE

            // Create a plot to display the health parameters over time
            val plot = binding.plot

            // Set plot series based on the health parameters
            for (param in plantParams) {
                seriesNitrogen.add(param.nitrogen)
                seriesPhosphorus.add(param.phosphorus)
                seriesPotassium.add(param.potassium)
                seriesPh.add(param.ph)
                seriesTemperature.add(param.temperature)
                seriesHumidity.add(param.humidity)
                seriesLight.add(param.lightLevel)
                param.dateTime?.let {
                    datesLabels.add(
                        DateTimeHandler.getDayMonthFromString(it)
                    )
                }
            }

            // Create series for the health parameters
            val nitrogenS = SimpleXYSeries(
                seriesNitrogen,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "N"
            )
            val phosphorusS = SimpleXYSeries(
                seriesPhosphorus,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "P"
            )
            val potassiumS = SimpleXYSeries(
                seriesPotassium,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "K"
            )
            val phS = SimpleXYSeries(
                seriesPh,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "Ph"
            )
            val temperatureS = SimpleXYSeries(
                seriesTemperature,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "T"
            )
            val humidityS = SimpleXYSeries(
                seriesHumidity,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "RH"
            )
            val lightS = SimpleXYSeries(
                seriesLight,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "L"
            )

            // Set plot format for the health parameters series
            val seriesNitrogenFormat = LineAndPointFormatter(Color.RED, Color.RED, null, null)
            val seriesPhosphorusFormat = LineAndPointFormatter(Color.BLUE, Color.BLUE, null, null)
            val seriesPotassiumFormat =
                LineAndPointFormatter(Color.YELLOW, Color.YELLOW, null, null)
            val seriesPhFormat = LineAndPointFormatter(Color.CYAN, Color.CYAN, null, null)
            val seriesTemperatureFormat =
                LineAndPointFormatter(Color.GREEN, Color.GREEN, null, null)
            val seriesHumidityFormat =
                LineAndPointFormatter(Color.MAGENTA, Color.MAGENTA, null, null)
            val seriesLightFormat = LineAndPointFormatter(Color.GRAY, Color.GRAY, null, null)

            // Add the health parameters series to the plot
            plot.addSeries(nitrogenS, seriesNitrogenFormat)
            plot.addSeries(phosphorusS, seriesPhosphorusFormat)
            plot.addSeries(potassiumS, seriesPotassiumFormat)
            plot.addSeries(phS, seriesPhFormat)
            plot.addSeries(temperatureS, seriesTemperatureFormat)
            plot.addSeries(humidityS, seriesHumidityFormat)
            plot.addSeries(lightS, seriesLightFormat)

            // Set the plot title and labels
            plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
                // Format x-axis labels to display the dates
                override fun format(
                    obj: Any?,
                    toAppendTo: StringBuffer,
                    pos: FieldPosition
                ): StringBuffer {
                    val i = (obj as Number).toFloat().roundToInt()
                    return toAppendTo.append(datesLabels[i])
                }

                // Parse x-axis labels
                override fun parseObject(source: String?, pos: ParsePosition): Any? {
                    return null
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind the view to avoid memory leaks
        _binding = null
    }
}