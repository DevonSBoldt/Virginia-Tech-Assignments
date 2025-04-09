package edu.vt.cs5254.bucketlist

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.vt.cs5254.bucketlist.GoalNoteType.*
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalDetailFragmentTest {

    private lateinit var scenario: FragmentScenario<GoalDetailFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer()
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun initialTitleAndButtons() {
        onView(withId(R.id.title_text))
            .check(matches(withText("My New Goal")))
        onView(withId(R.id.note_0_button))
            .check(matches(withText("First Step")))
        onView(withId(R.id.note_1_button))
            .check(matches(withText("Second Step")))
        onView(withId(R.id.note_2_button))
            .check(matches(withText(PAUSED.toString())))
        onView(withId(R.id.note_3_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.note_4_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun initialCheckboxes() {
        onView(withId(R.id.paused_checkbox))
            .check(matches(isChecked()))
            .check(matches(isEnabled()))
        onView(withId(R.id.completed_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun uncheckPaused() {
        onView(withId(R.id.paused_checkbox))
            .perform(click())
            .check(matches(not(isChecked())))
            .check(matches(isEnabled()))
        onView(withId(R.id.completed_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(isEnabled()))
        onView(withId(R.id.note_2_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun uncheckPausedCheckPaused() {
        onView(withId(R.id.paused_checkbox))
            .perform(click())
            .perform(click())
            .check(matches(isChecked()))
            .check(matches(isEnabled()))
        onView(withId(R.id.completed_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(not(isEnabled())))
        onView(withId(R.id.note_2_button))
            .check(matches(withText(PAUSED.toString())))
    }

    @Test
    fun uncheckPausedCheckCompleted() {
        onView(withId(R.id.paused_checkbox))
            .perform(click())
        onView(withId(R.id.completed_checkbox))
            .perform(click())
            .check(matches(isChecked()))
            .check(matches(isEnabled()))
        onView(withId(R.id.paused_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(not(isEnabled())))
        onView(withId(R.id.note_2_button))
            .check(matches(withText(COMPLETED.toString())))
    }

    @Test
    fun uncheckPausedCheckCompletedUncheckCompleted() {
        onView(withId(R.id.paused_checkbox))
            .perform(click())
        onView(withId(R.id.completed_checkbox))
            .perform(click())
            .perform(click())
            .check(matches(not(isChecked())))
            .check(matches(isEnabled()))
        onView(withId(R.id.paused_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(isEnabled()))
        onView(withId(R.id.note_2_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun goalInViewModelChangedByEditText() {
        scenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            assertEquals("My New Goal", gdfVM.goal.title)
        }

        onView(withId(R.id.title_text))
            .perform(replaceText("Different Goal"))

        scenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            assertEquals("Different Goal", gdfVM.goal.title)
        }
    }

    @Test
    fun goalInViewModelChangedByCheckboxes() {
        scenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            assertFalse(gdfVM.goal.isCompleted)
            assertTrue(gdfVM.goal.isPaused)
            assertEquals(3, gdfVM.goal.notes.size)
            assertTrue(gdfVM.goal.notes[2].text.isEmpty())
            assertEquals(PAUSED, gdfVM.goal.notes[2].type)
        }

        onView(withId(R.id.paused_checkbox))
            .perform(click())

        scenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            assertFalse(gdfVM.goal.isCompleted)
            assertFalse(gdfVM.goal.isPaused)
            assertEquals(2, gdfVM.goal.notes.size)
        }

        onView(withId(R.id.completed_checkbox))
            .perform(click())

        scenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            assertTrue(gdfVM.goal.isCompleted)
            assertFalse(gdfVM.goal.isPaused)
            assertEquals(3, gdfVM.goal.notes.size)
            assertTrue(gdfVM.goal.notes[2].text.isEmpty())
            assertEquals(COMPLETED, gdfVM.goal.notes[2].type)
        }

        onView(withId(R.id.completed_checkbox))
            .perform(click())

        scenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            assertFalse(gdfVM.goal.isCompleted)
            assertFalse(gdfVM.goal.isPaused)
            assertEquals(2, gdfVM.goal.notes.size)
        }
    }

    @Test
    fun changesPersistAfterRotation() {
        onView(withId(R.id.paused_checkbox))
            .perform(click())
        onView(withId(R.id.completed_checkbox))
            .perform(click())
        onView(withId(R.id.title_text))
            .perform(replaceText("Another Goal"))

        scenario.recreate()

        onView(withId(R.id.paused_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(not(isEnabled())))
        onView(withId(R.id.completed_checkbox))
            .check(matches(isChecked()))
            .check(matches(isEnabled()))
        onView(withId(R.id.title_text))
            .check(matches(withText("Another Goal")))
        onView(withId(R.id.note_2_button))
            .check(matches(withText(COMPLETED.toString())))

        scenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            assertTrue(gdfVM.goal.isCompleted)
            assertFalse(gdfVM.goal.isPaused)
            assertEquals("Another Goal", gdfVM.goal.title)
            assertEquals(3, gdfVM.goal.notes.size)
            assertTrue(gdfVM.goal.notes[2].text.isEmpty())
            assertEquals(COMPLETED, gdfVM.goal.notes[2].type)
        }
    }

    @Test
    fun displayFiveNotes() {

        val localScenario: FragmentScenario<GoalDetailFragment> = launchFragmentInContainer(
            initialState = Lifecycle.State.CREATED
        )

        localScenario.withFragment {
            val gdfVM: GoalDetailViewModel by viewModels()
            gdfVM.goal = gdfVM.goal.copy(title = "A Very Big Goal").apply {
                notes = listOf(
                    GoalNote(type = PROGRESS, text = "Aaa", goalId = gdfVM.goal.id),
                    GoalNote(type = PROGRESS, text = "Bbb", goalId = gdfVM.goal.id),
                    GoalNote(type = PROGRESS, text = "Ccc", goalId = gdfVM.goal.id),
                    GoalNote(type = PROGRESS, text = "Ddd", goalId = gdfVM.goal.id),
                    GoalNote(type = COMPLETED, goalId = gdfVM.goal.id)
                )
            }
        }

        localScenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.title_text))
            .check(matches(withText("A Very Big Goal")))
        onView(withId(R.id.note_0_button))
            .check(matches(withText("Aaa")))
        onView(withId(R.id.note_1_button))
            .check(matches(withText("Bbb")))
        onView(withId(R.id.note_2_button))
            .check(matches(withText("Ccc")))
        onView(withId(R.id.note_3_button))
            .check(matches(withText("Ddd")))
        onView(withId(R.id.note_4_button))
            .check(matches(withText(COMPLETED.toString())))
        onView(withId(R.id.paused_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(not(isEnabled())))
        onView(withId(R.id.completed_checkbox))
            .check(matches(isChecked()))
            .check(matches(isEnabled()))

        localScenario.close()
    }
}

