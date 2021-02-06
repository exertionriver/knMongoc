package libmongoc

import kotlinx.serialization.Serializable
import profile.Profile

@Serializable
class SerializableProfile private constructor(val uuid : String, val profileName : String, val serializableEarthLocation : SerializableEarthLocation) {

    @ExperimentalUnsignedTypes
    constructor(profile : Profile
        , updUuId : String = Uuid.getNewUuid()
        , updProfileName : String = profile.profileName
        , updSerializableEarthLocation : SerializableEarthLocation = SerializableEarthLocation(profile.earthLocation)
    ) : this (
        uuid = updUuId
        , profileName = updProfileName
        , serializableEarthLocation = updSerializableEarthLocation
    )

    override fun toString() = "SerializableProfile(uuid=$uuid, profileName='$profileName', serializableEarthLocation=$serializableEarthLocation)"
}