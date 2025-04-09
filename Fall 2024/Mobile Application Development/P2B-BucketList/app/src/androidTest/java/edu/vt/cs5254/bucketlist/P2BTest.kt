package edu.vt.cs5254.bucketlist

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import java.util.regex.Pattern

private const val GOAL_LAST_UPDATED_PATTERN = "Last updated 2024-10-01 at ..:[03]1:24 [AP]M"

@RunWith(AndroidJUnit4::class)
class P2BTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        scenario = launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun initialViewShowsFirstEightFromPrePopulatedData() {

        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(0, hasDescendant(withText("Learn mobile app development")))))
            .check(matches(atPosition(1, hasDescendant(withText("See aurora borealis/australis")))))
            .check(matches(atPosition(2, hasDescendant(withText("Ride in a hot air balloon")))))
            .check(matches(atPosition(3, hasDescendant(withText("Foster or adopt a pet")))))
            .check(matches(atPosition(4, hasDescendant(withText("Earn a graduate degree")))))
            .check(matches(atPosition(5, hasDescendant(withText("Try skydiving")))))
            .check(matches(atPosition(6, hasDescendant(withText("Swim with dolphins")))))
            .check(matches(atPosition(7, hasDescendant(withText("Start a fire without matches")))))
    }

    @Test
    fun scrollingToBottomShowsLastEightFromPrePopulatedData() {
        onView(withId(R.id.goal_recycler_view))
            .perform(scrollToPosition<GoalHolder>(19))
            .check(matches(atPosition(12, hasDescendant(withText("Write a book")))))
            .check(matches(atPosition(13, hasDescendant(withText("Visit Stonehenge")))))
            .check(matches(atPosition(14, hasDescendant(withText("Knit a scarf")))))
            .check(matches(atPosition(15, hasDescendant(withText("Try snow skiing")))))
            .check(matches(atPosition(16, hasDescendant(withText("Visit the Pyramids of Giza")))))
            .check(matches(atPosition(17, hasDescendant(withText("Learn how to juggle")))))
            .check(matches(atPosition(18, hasDescendant(withText("Tour the Tower of London")))))
            .check(matches(atPosition(19, hasDescendant(withText("Develop a BucketList app")))))
    }

    @Test
    fun goalFourCheckDetails() {
        onView(withText("Earn a graduate degree"))
            .perform(click())
        onView(withId(R.id.title_text))
            .check(matches(withText("Earn a graduate degree")))
        onView(withId(R.id.last_updated_text))
            .check(matches(withPattern(GOAL_LAST_UPDATED_PATTERN)))
        checkNoteGone(0)
        onView(withId(R.id.paused_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(isEnabled()))
        onView(withId(R.id.completed_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(isEnabled()))
    }

    @Test
    fun goalFourRotateClickPauseThenBackRotateAndConfirmList() {
        onView(withText("Earn a graduate degree"))
            .perform(click())
        scenario.recreate()
        changeCheckboxState(CHECKBOX.PAUSED, true)
        checkVisibleNoteText(0, "PAUSED")
        checkNoteGone(1)
        onView(withId(R.id.last_updated_text))
            .check(matches(not(withPattern(GOAL_LAST_UPDATED_PATTERN))))
        pressBack()
        scenario.recreate()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0,
                        allOf(
                            hasDescendant(withText("Earn a graduate degree")),
                            hasDescendant(withText("Progress: 0")),
                            hasDescendant(
                                allOf(
                                    withId(R.id.list_item_image),
                                    matchesDrawable(R.drawable.ic_goal_paused),
                                    withEffectiveVisibility(Visibility.VISIBLE)
                                )
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun goalThreeNoImageInList() {
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        3,
                        allOf(
                            hasDescendant(withText("Foster or adopt a pet")),
                            hasDescendant(withText("Progress: 3")),
                            hasDescendant(
                                allOf(
                                    withId(R.id.list_item_image),
                                    withEffectiveVisibility(Visibility.GONE)
                                )
                            )
                        )
                    )
                )
            )
    }
    @Test
    fun goalNineChangeTitleClickCompletedThenBackAndConfirmList() {
        onView(withText("Try water skiing"))
            .perform(click())
        changeCheckboxState(CHECKBOX.COMPLETED, true)
        onView(withId(R.id.title_text))
            .perform(replaceText("Try surfing"))
            .perform(closeSoftKeyboard())
        checkVisibleNoteText(0, "Step One")
        checkVisibleNoteText(1, "COMPLETED")
        checkNoteGone(2)
        onView(withId(R.id.last_updated_text))
            .check(matches(not(withPattern(GOAL_LAST_UPDATED_PATTERN))))
        onView(withId(R.id.title_text))
            .check(matches(withText("Try surfing")))
        pressBack()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0,
                        allOf(
                            hasDescendant(withText("Try surfing")),
                            hasDescendant(withText("Progress: 1")),
                            hasDescendant(
                                allOf(
                                    withId(R.id.list_item_image),
                                    matchesDrawable(R.drawable.ic_goal_completed),
                                    withEffectiveVisibility(Visibility.VISIBLE)
                                )
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun goalTwoThenBackThenGoalTwoCheckSameLastUpdated() {
        onView(withText("Ride in a hot air balloon"))
            .perform(click())
        onView(withId(R.id.title_text))
            .check(matches(withText("Ride in a hot air balloon")))
        checkVisibleNoteText(0, "Step One")
        checkVisibleNoteText(1, "Step Two")
        checkVisibleNoteText(2, "PAUSED")
        checkNoteGone(3)
        pressBack()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        2, allOf(
                            hasDescendant(withText("Ride in a hot air balloon")),
                            hasDescendant(withText("Progress: 2"))
                        )
                    )
                )
            )
        onView(withText("Ride in a hot air balloon"))
            .perform(click())
        onView(withId(R.id.last_updated_text))
            .check(matches(withPattern(GOAL_LAST_UPDATED_PATTERN)))
    }

    @Test
    fun goalSixFabHiddenUntilUncheckCompleted() {
        onView(withText("Swim with dolphins"))
            .perform(click())
        onView(withId(R.id.title_text))
            .check(matches(withText("Swim with dolphins")))
        checkVisibleNoteText(0, "Step One")
        checkVisibleNoteText(1, "Step Two")
        checkVisibleNoteText(2, "COMPLETED")
        checkNoteGone(3)
        onView(withId(R.id.add_progress_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        changeCheckboxState(CHECKBOX.COMPLETED, false)
        checkNoteGone(2)
        onView(withId(R.id.add_progress_button))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun goalTwoAddProgressAfterPausedThenUncheckRecheckPaused() {
        onView(withText("Ride in a hot air balloon"))
            .perform(click())
        checkVisibleNoteText(0, "Step One")
        checkVisibleNoteText(1, "Step Two")
        checkVisibleNoteText(2, "PAUSED")
        checkNoteGone(3)
        onView(withId(R.id.paused_checkbox))
            .check(matches(isChecked()))
            .check(matches(isEnabled()))
        onView(withId(R.id.completed_checkbox))
            .check(matches(not(isChecked())))
            .check(matches(not(isEnabled())))
        addProgress("Identified a facility")
        checkVisibleNoteText(2, "PAUSED")
        checkVisibleNoteText(3, "Identified a facility")
        changeCheckboxState(CHECKBOX.PAUSED, false)
        checkVisibleNoteText(2, "Identified a facility")
        checkNoteGone(3)
        changeCheckboxState(CHECKBOX.PAUSED, true)
        checkVisibleNoteText(2, "Identified a facility")
        checkVisibleNoteText(3, "PAUSED")
    }

    @Test
    fun goalFourAddFiveProgressAndCheckList() {
        onView(withText("Earn a graduate degree"))
            .perform(click())
        checkNoteGone(0)
        addProgress("First Step")
        addProgress("Second Step")
        addProgress("Third Step")
        addProgress("Fourth Step")
        addProgress("Fifth Step")
        checkVisibleNoteText(0, "First Step")
        checkVisibleNoteText(1, "Second Step")
        checkVisibleNoteText(2, "Third Step")
        checkVisibleNoteText(3, "Fourth Step")
        checkVisibleNoteText(4, "Fifth Step")
        pressBack()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0, allOf(
                            hasDescendant(withText("Earn a graduate degree")),
                            hasDescendant(withText("Progress: 5"))
                        )
                    )
                )
            )
    }

    @Test
    fun goalEightUncheckPausedCheckCompletedConfirmLastUpdated() {
        onView(withText("Travel to every continent"))
            .perform(click())
        onView(withId(R.id.last_updated_text))
            .check(matches(withPattern(GOAL_LAST_UPDATED_PATTERN)))
        val timestamp0 = getText(withId(R.id.last_updated_text))
        changeCheckboxState(CHECKBOX.PAUSED, false)
        val timestamp1 = getText(withId(R.id.last_updated_text))
        assertNotEquals(timestamp0, timestamp1) //######
        sleep(1000)
        changeCheckboxState(CHECKBOX.COMPLETED, true)
        val timestamp2 = getText(withId(R.id.last_updated_text))
        assertNotEquals(timestamp1, timestamp2)
    }

    @Test
    fun goalSevenChangeTitleCheckLastUpdatedRotateBackCheckList() {
        onView(withText("Start a fire without matches"))
            .perform(click())
        onView(withId(R.id.title_text))
            .perform(replaceText("Start a fire"))
        scenario.recreate()
        onView(withId(R.id.last_updated_text))
            .check(matches(not(withPattern(GOAL_LAST_UPDATED_PATTERN))))
        pressBack()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0, allOf(
                            hasDescendant(withText("Start a fire")),
                            hasDescendant(withText("Progress: 3"))
                        )
                    )
                )
            )
        scenario.recreate()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0, allOf(
                            hasDescendant(withText("Start a fire")),
                            hasDescendant(withText("Progress: 3"))
                        )
                    )
                )
            )
    }

    @Test
    fun goalEighteenAddTwoProgressCheckLastUpdatedRotateBackCheckList() {
        onView(withId(R.id.goal_recycler_view)).perform(scrollToPosition<GoalHolder>(19))
        onView(withText("Tour the Tower of London"))
            .perform(click())
        addProgress("extra one")
        addProgress("extra two")
        scenario.recreate()

        onView(withId(R.id.last_updated_text))
            .check(matches(not(withPattern(GOAL_LAST_UPDATED_PATTERN))))
        pressBack()
        onView(withId(R.id.goal_recycler_view)).perform(scrollToPosition<GoalHolder>(0))
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0, allOf(
                            hasDescendant(withText("Tour the Tower of London")),
                            hasDescendant(withText("Progress: 4"))
                        )
                    )
                )
            )
        scenario.recreate()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0, allOf(
                            hasDescendant(withText("Tour the Tower of London")),
                            hasDescendant(withText("Progress: 4"))
                        )
                    )
                )
            )
    }

    @Test
    fun goalTwoExceedFiveProgressNoDisplayButProgressCountCorrect() {
        onView(withText("Ride in a hot air balloon"))
            .perform(click())
        addProgress("Step Three")
        addProgress("Step Four")
        addProgress("Step Five")
        addProgress("Step Six")
        checkVisibleNoteText(0, "Step One")
        checkVisibleNoteText(1, "Step Two")
        checkVisibleNoteText(2, "PAUSED")
        checkVisibleNoteText(3, "Step Three")
        checkVisibleNoteText(4, "Step Four")
        scenario.recreate()
        checkVisibleNoteText(0, "Step One")
        checkVisibleNoteText(1, "Step Two")
        checkVisibleNoteText(2, "PAUSED")
        checkVisibleNoteText(3, "Step Three")
        checkVisibleNoteText(4, "Step Four")
        pressBack()
        onView(withId(R.id.goal_recycler_view))
            .check(
                matches(
                    atPosition(
                        0, allOf(
                            hasDescendant(withText("Ride in a hot air balloon")),
                            hasDescendant(withText("Progress: 6"))
                        )
                    )
                )
            )
    }

    @Test
    fun reverseOrderOfGoalsZeroOneTwo() {
        onView(withText("See aurora borealis/australis"))
            .perform(click())
        changeCheckboxState(CHECKBOX.PAUSED, true)
        pressBack()
        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(0, hasDescendant(withText("See aurora borealis/australis")))))
            .check(matches(atPosition(1, hasDescendant(withText("Learn mobile app development")))))
            .check(matches(atPosition(2, hasDescendant(withText("Ride in a hot air balloon")))))
        onView(withText("Ride in a hot air balloon"))
            .perform(click())
        changeCheckboxState(CHECKBOX.PAUSED, false)
        pressBack()
        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(0, hasDescendant(withText("Ride in a hot air balloon")))))
            .check(matches(atPosition(1, hasDescendant(withText("See aurora borealis/australis")))))
            .check(matches(atPosition(2, hasDescendant(withText("Learn mobile app development")))))
    }


    //  ------ PRIVATE HELPER METHODS BELOW HERE ------

    private fun matchesDrawable(resourceID: Int): Matcher<View?> {
        return object : BoundedMatcher<View?, ImageView>(ImageView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("an ImageView with resourceID: ")
                description.appendValue(resourceID)
            }

            override fun matchesSafely(imageView: ImageView): Boolean {
                val expBM = imageView.context.resources
                    .getDrawable(resourceID, null).toBitmap()
                return imageView.drawable?.toBitmap()?.sameAs(expBM) ?: false
            }
        }
    }

    private fun withPattern(regex: String): Matcher<View?> {
        return object : BoundedMatcher<View?, TextView>(TextView::class.java) {
            private val pattern = Pattern.compile(regex)
            override fun describeTo(description: Description) {
                description.appendText("a TextView with text matching regex: /$regex/")
            }
            override fun matchesSafely(item: TextView?): Boolean {
                return item?.text?.let {
                    pattern.matcher(it).matches()
                } ?: false
            }
        }
    }

    private fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("has item at position $position: ")
                itemMatcher.describeTo(description)
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val viewHolder = view.findViewHolderForAdapterPosition(position) ?: return false
                return itemMatcher.matches(viewHolder.itemView)
            }
        }
    }

    private fun addProgress(progressText: String) {
        onView(withId(R.id.add_progress_button))
            .perform(click())
        onView(withId(R.id.progress_text))
            .perform(replaceText(progressText))
        onView(withText("Add"))
            .inRoot(isDialog())
            .perform(click())
    }

    @IdRes
    private fun noteIdForNum(noteNum: Int): Int {
        return listOf(
            R.id.note_0_button,
            R.id.note_1_button,
            R.id.note_2_button,
            R.id.note_3_button,
            R.id.note_4_button,
        )[noteNum]
    }

    private fun checkVisibleNoteText(noteNum: Int, expectedText: String) {
        onView(withId(noteIdForNum(noteNum)))
            .check(matches(withText(expectedText)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    private fun checkNoteGone(noteNum: Int) {
        onView(withId(noteIdForNum(noteNum)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    enum class CHECKBOX(@IdRes val id: Int) {
        PAUSED(R.id.paused_checkbox),
        COMPLETED(R.id.completed_checkbox);

        fun other(): CHECKBOX = when (this) {
            PAUSED -> COMPLETED
            COMPLETED -> PAUSED
        }

    }

    private fun changeCheckboxState(checkBox: CHECKBOX, newCheckedState: Boolean) {
        val thisCB = onView(withId(checkBox.id))
        val thatCB = onView(withId(checkBox.other().id))
        if (newCheckedState) {
            thisCB.check(matches(isEnabled()))
            thisCB.check(matches(not(isChecked())))
            thisCB.perform(click())
            thisCB.check(matches(isChecked()))
            thisCB.check(matches(isEnabled()))
            thatCB.check(matches(not(isChecked())))
            thatCB.check(matches(not(isEnabled())))
        } else {
            thisCB.check(matches(isEnabled()))
            thisCB.check(matches(isChecked()))
            thisCB.perform(click())
            thisCB.check(matches(not(isChecked())))
            thisCB.check(matches(isEnabled()))
            thatCB.check(matches(not(isChecked())))
            thatCB.check(matches(isEnabled()))
        }
    }

    private fun getText(matcher: Matcher<View>): String {
        var text = ""
        onView(matcher).perform(
            object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isAssignableFrom(TextView::class.java)
                }

                override fun getDescription(): String {
                    return "Fetching text from TextView"
                }

                override fun perform(uiController: UiController?, view: View?) {
                    text = (view as TextView).text.toString()
                }
            }
        )
        return text
    }
}