/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

  @Test
  public void testHasAnyGroup() {
    User user = User.newBuilder().name("foo").build();
    assertTrue(user.hasGroup(null));
    assertTrue(user.hasGroup(""));
    user = User.newBuilder().name("foo").groups("group1", "group2").build();
    assertTrue(user.hasGroup(null));
    assertTrue(user.hasGroup(""));
  }

  @Test
  public void testHasSomeGroup() {
    User user = User.newBuilder().name("foo").groups("group1", "group2").build();
    assertTrue(user.hasGroup("group1"));
  }

  @Test
  public void testHasNotSomeGroup() {
    User user = User.newBuilder().name("foo").groups("group1", "group2").build();
    assertFalse(user.hasGroup("group3"));
    user = User.newBuilder().name("foo").build();
    assertFalse(user.hasGroup("group3"));
  }

  @Test
  public void testHasOneOfGroup() {
    User user = User.newBuilder().name("foo").groups("group1").build();
    assertTrue(user.hasOneOfGroup("group1", "group2"));
  }

  @Test
  public void testHasAnyOneOfGroup() {
    User user = User.newBuilder().name("foo").groups("group1").build();
    assertTrue(user.hasOneOfGroup((List<String>)null));
    assertTrue(user.hasOneOfGroup(new ArrayList<>()));
    assertTrue(user.hasOneOfGroup());
    assertTrue(user.hasOneOfGroup((String[]) null));
  }

  @Test
  public void testHasNotOneOfGroup() {
    User user = User.newBuilder().name("foo").groups("group3").build();
    assertFalse(user.hasOneOfGroup("group1", "group2"));
    user = User.newBuilder().name("foo").build();
    assertFalse(user.hasOneOfGroup("group1", "group2"));
  }

  @Test
  public void testHasApplication() {
    User user = User.newBuilder().name("foo").applications("application1").build();
    assertTrue(user.hasApplication("application1"));
    assertFalse(user.hasApplication("application2"));
    user = User.newBuilder().name("foo").build();
    assertFalse(user.hasApplication("application1"));
  }

  @Test
  public void testBuilderFromEmail() {
    String email = "andy.warhol@factory.org";
    User user = User.newBuilder(email).build();
    assertEquals(email, user.getEmail());
    assertEquals("andy.warhol", user.getName());
    assertEquals("Andy", user.getFirstName());
    assertEquals("Warhol", user.getLastName());

    email = "andy@factory.org";
    user = User.newBuilder(email).build();
    assertEquals(email, user.getEmail());
    assertEquals("andy", user.getName());
    assertEquals("Andy", user.getFirstName());
    assertEquals("", user.getLastName());
  }

}
