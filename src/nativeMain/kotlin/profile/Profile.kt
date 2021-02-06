package profile

import libmongoc.SerializableProfile

@ExperimentalUnsignedTypes
data class Profile constructor(val profileName : String, val earthLocation: EarthLocation) {

    constructor(serializableProfile: SerializableProfile) : this (
        profileName = serializableProfile.profileName
        , earthLocation = EarthLocation(serializableProfile.serializableEarthLocation)
    )

    override fun toString() = "Profile(profileName=$profileName, earthLocation=$earthLocation)"

}