package cu.edu.inca.abonosverdes.ui.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey, Parcelable {
    @Parcelize
    @Serializable
    data object Splash : Destination

    @Parcelize
    @Serializable
    data object Onboarding : Destination

    @Parcelize
    @Serializable
    data object Home : Destination

    @Parcelize
    @Serializable
    data object Cultivos : Destination

    @Parcelize
    @Serializable
    data object Fincas : Destination

    @Parcelize
    @Serializable
    data object Fertilizantes : Destination

    @Parcelize
    @Serializable
    data object Calculadora : Destination

    @Parcelize
    @Serializable
    data object Guia : Destination
}
