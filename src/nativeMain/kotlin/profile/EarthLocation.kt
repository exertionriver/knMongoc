package profile

import kotlinx.datetime.*
import libmongoc.SerializableEarthLocation

@ExperimentalUnsignedTypes
data class EarthLocation(val longitude : Double, val latitude : Double, val altitude : Double
    , val timeZone : TimeZone = TimeZone.currentSystemDefault()
    , val utcDateTime : LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC) ) {

    constructor(serializableEarthLocation: SerializableEarthLocation) : this(
        longitude = serializableEarthLocation.longitude
        , latitude = serializableEarthLocation.latitude
        , altitude = serializableEarthLocation.altitude
        , timeZone = TimeZone.of(serializableEarthLocation.timeZoneStr)
        , utcDateTime = LocalDateTime.parse(serializableEarthLocation.utcDateTimeStr) )

    override fun toString() = "EarthLocation(longitude=$longitude, latitude=$latitude, altitude=$altitude, timeZone=$timeZone, utcDateTime=$utcDateTime)"
}