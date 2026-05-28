package com.sergio.retrodex;

import android.Manifest;
import android.content.Context;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RetroDexE2ETest {

    private final GrantPermissionRule notificationPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);
    private final ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public RuleChain rules = RuleChain
            .outerRule(notificationPermissionRule)
            .around(activityRule);

    @Before
    public void disableRemoteNotifications() {
        Context context = ApplicationProvider.getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("notifications_enabled", false)
                .commit();
    }

    @Test
    public void filtersCatalogByDecadeLikeAUser() {
        onView(withId(R.id.spinner_filter)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("80s"))).perform(click());

        onView(withId(R.id.recycler_characters))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("Mario"))));
        onView(withText("Mario")).check(matches(isDisplayed()));
    }

    @Test
    public void createsCharacterFromTheMainAddButton() {
        String name = uniqueName("E2E Hero");

        createCharacter(name, "Arcade Test", "Personaje creado desde Espresso");

        onView(withId(R.id.recycler_characters))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(name))));
        onView(withText(name)).check(matches(isDisplayed()));
    }

    @Test
    public void opensNewCharacterDetailAfterCreatingIt() {
        String name = uniqueName("E2E Detail");
        String origin = "Retro Lab";
        String description = "Detalle verificado por una prueba E2E";

        createCharacter(name, origin, description);

        onView(withId(R.id.recycler_characters))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(name)), click()));

        onView(withId(R.id.tv_detail_name)).check(matches(withText(name)));
        onView(withId(R.id.tv_detail_origin)).check(matches(withText(origin)));
        onView(withId(R.id.tv_detail_description)).check(matches(withText(description)));
    }

    private void createCharacter(String name, String origin, String description) {
        onView(withId(R.id.fab_add)).perform(click());

        onView(withId(R.id.et_name)).perform(
                ViewActions.scrollTo(),
                typeText(name),
                closeSoftKeyboard());
        onView(withId(R.id.et_origin)).perform(
                ViewActions.scrollTo(),
                typeText(origin),
                closeSoftKeyboard());
        onView(withId(R.id.et_description)).perform(
                ViewActions.scrollTo(),
                typeText(description),
                closeSoftKeyboard());
        onView(withId(R.id.btn_save_character)).perform(ViewActions.scrollTo(), click());
    }

    private String uniqueName(String prefix) {
        return prefix + " " + System.currentTimeMillis();
    }
}
