package com.openmobilehub.android.storage.plugin.dropbox.testdoubles

import com.dropbox.core.v2.teamcommon.MemberSpaceLimitType
import com.dropbox.core.v2.users.IndividualSpaceAllocation
import com.dropbox.core.v2.users.SpaceAllocation
import com.dropbox.core.v2.users.SpaceUsage
import com.dropbox.core.v2.users.TeamSpaceAllocation
import io.mockk.every

fun SpaceUsage.setUpMockForPersonalAccount() {
    every { used } returns 100L
    every { allocation } returns SpaceAllocation.individual(
        IndividualSpaceAllocation(104857600L)
    )
}

fun SpaceUsage.setupMockForTeamAccount() {
    every { used } returns 1000L
    // We only read value from getUsed(). Doesn't matter the used value at TeamSpaceAllocation
    every { allocation } returns SpaceAllocation.team(
        TeamSpaceAllocation(
            12345L,
            1048576000L,
            100000L,
            MemberSpaceLimitType.OFF,
            0L
        )
    )
}

fun SpaceUsage.setupMockForOtherAccount() {
    every { used } returns 10000L
    every { allocation } returns SpaceAllocation.OTHER
}
