package world.bentobox.bentobox.api.commands.island.team;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.common.collect.ImmutableSet;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.island.team.Invite.Type;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.RanksManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class })
public class IslandTeamCommandTest {

    @Mock
    private CompositeCommand ic;


    private IslandTeamCommand tc;

    private UUID uuid;

    private UUID invitee;

    @Mock
    private IslandsManager im;

    @Mock
    private User user;

    @Mock
    private World world;

    @Mock
    private PluginManager pim;

    @Mock
    private IslandWorldManager iwm;

    @Mock
    private @Nullable Island island;

    /**
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Parent command
        when(ic.getPermissionPrefix()).thenReturn("bskyblock.");
        when(ic.getWorld()).thenReturn(world);

        // user
        uuid = UUID.randomUUID();
        invitee = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPermissionValue(eq("bskyblock.team.maxsize"), anyInt())).thenReturn(3);

        // island Manager
        when(plugin.getIslands()).thenReturn(im);
        // is owner of island
        when(im.getOwner(any(), any())).thenReturn(uuid);
        when(im.getPrimaryIsland(world, uuid)).thenReturn(island);
        // Max members
        when(im.getMaxMembers(eq(island), eq(RanksManager.MEMBER_RANK))).thenReturn(3);
        // No team members
        when(im.getMembers(any(), any(UUID.class))).thenReturn(Collections.emptySet());
        // Add members
        ImmutableSet<UUID> set = new ImmutableSet.Builder<UUID>().build();
        // No members
        when(island.getMemberSet(anyInt(), any(Boolean.class))).thenReturn(set);
        when(island.getMemberSet(anyInt())).thenReturn(set);
        when(island.getMemberSet()).thenReturn(set);
        when(island.getOwner()).thenReturn(uuid);
        // island
        when(im.getIsland(any(), eq(uuid))).thenReturn(island);

        // Bukkit
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getPluginManager()).thenReturn(pim);

        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("bskyblock.");


        // Command under test
        tc = new IslandTeamCommand(ic);
    }

    /**
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#IslandTeamCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    public void testIslandTeamCommand() {
        assertEquals("team", tc.getLabel());
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("bskyblock.island.team", tc.getPermission());
        assertTrue(tc.isOnlyPlayer());
        assertEquals("commands.island.team.description", tc.getDescription());

    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringNoIsland() {
        when(im.getPrimaryIsland(world, uuid)).thenReturn(null);
        assertFalse(tc.execute(user, "team", Collections.emptyList()));
        verify(user).sendMessage(eq("general.errors.no-island"));
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringIslandIsNotFull() {
        assertTrue(tc.execute(user, "team", Collections.emptyList()));
        verify(user).sendMessage(eq("commands.island.team.invite.you-can-invite"), eq(TextVariables.NUMBER), eq("3"));
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringIslandIsFull() {
        // Max members
        when(im.getMaxMembers(eq(island), eq(RanksManager.MEMBER_RANK))).thenReturn(0);
        assertTrue(tc.execute(user, "team", Collections.emptyList()));
        verify(user).sendMessage(eq("commands.island.team.invite.errors.island-is-full"));
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#addInvite(world.bentobox.bentobox.api.commands.island.team.Invite.Type, java.util.UUID, java.util.UUID)}.
     */
    @Test
    public void testAddInvite() {
        tc.addInvite(Invite.Type.TEAM, uuid, invitee, island);
        assertTrue(tc.isInvited(invitee));
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#isInvited(java.util.UUID)}.
     */
    @Test
    public void testIsInvited() {
        assertFalse(tc.isInvited(invitee));
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#getInviter(java.util.UUID)}.
     */
    @Test
    public void testGetInviter() {
        tc.addInvite(Invite.Type.TEAM, uuid, invitee, island);
        assertEquals(uuid, tc.getInviter(invitee));
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#getInviter(java.util.UUID)}.
     */
    @Test
    public void testGetInviterNoInvite() {
        assertNull(tc.getInviter(invitee));
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#getInvite(java.util.UUID)}.
     */
    @Test
    public void testGetInvite() {
        assertNull(tc.getInvite(invitee));
        tc.addInvite(Invite.Type.TEAM, uuid, invitee, island);
        @Nullable
        Invite invite = tc.getInvite(invitee);
        assertEquals(invitee, invite.getInvitee());
        assertEquals(Type.TEAM, invite.getType());
        assertEquals(uuid, invite.getInviter());
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.island.team.IslandTeamCommand#removeInvite(java.util.UUID)}.
     */
    @Test
    public void testRemoveInvite() {
        assertNull(tc.getInvite(invitee));
        tc.addInvite(Invite.Type.TEAM, uuid, invitee, island);
        tc.removeInvite(invitee);
        assertNull(tc.getInvite(invitee));
    }
}
