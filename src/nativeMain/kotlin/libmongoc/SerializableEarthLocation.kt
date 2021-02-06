package libmongoc

import kotlinx.serialization.Serializable
import profile.EarthLocation

@Serializable
class SerializableEarthLocation private constructor (val uuid : String, val longitude : Double, val latitude : Double, val altitude : Double, val timeZoneStr : String, val utcDateTimeStr : String) {

    @ExperimentalUnsignedTypes
    constructor(earthLocation : EarthLocation
        , updUuId : String = Uuid.getNewUuid()
        , updLongitude : Double = earthLocation.longitude
        , updLatitude : Double = earthLocation.latitude
        , updAltitude : Double = earthLocation.altitude
        , updTimeZoneStr : String = earthLocation.timeZone.toString()
        , updUtcDateTimeStr : String = earthLocation.utcDateTime.toString()
    ) : this (
        uuid = updUuId
        , longitude = updLongitude
        , latitude = updLatitude
        , altitude = updAltitude
        , timeZoneStr = updTimeZoneStr
        , utcDateTimeStr = updUtcDateTimeStr
    )

    override fun toString() = "SerializableEarthLocation(uuid=$uuid, longitude=$longitude, latitude=$latitude, altitude=$altitude, timeZoneStr='$timeZoneStr', utcDateTimeStr='$utcDateTimeStr')"
}


