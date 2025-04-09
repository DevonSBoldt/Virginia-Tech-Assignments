package edu.vt.cs5254.bucketlist


import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.vt.cs5254.bucketlist.GoalNoteType.*
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class GoalListFragmentTest {

    private lateinit var scenario: FragmentScenario<GoalListFragment>

    @Before
    fun setUp() {
        scenario = FragmentScenario.launchInContainer(GoalListFragment::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun firstEightGoalsInitiallyVisible() {
        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(0, hasDescendant(withText("Goal #0")))))
            .check(matches(atPosition(1, hasDescendant(withText("Goal #1")))))
            .check(matches(atPosition(2, hasDescendant(withText("Goal #2")))))
            .check(matches(atPosition(3, hasDescendant(withText("Goal #3")))))
            .check(matches(atPosition(4, hasDescendant(withText("Goal #4")))))
            .check(matches(atPosition(5, hasDescendant(withText("Goal #5")))))
            .check(matches(atPosition(6, hasDescendant(withText("Goal #6")))))
            .check(matches(atPosition(7, hasDescendant(withText("Goal #7")))))
    }

    @Test
    fun goalNoteCountsDisplayedProperly() {
        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(3, hasDescendant(withText("Progress: 3")))))
            .check(matches(atPosition(4, hasDescendant(withText("Progress: 0")))))
            .check(matches(atPosition(5, hasDescendant(withText("Progress: 1")))))
            .check(matches(atPosition(6, hasDescendant(withText("Progress: 2")))))
            .check(matches(atPosition(7, hasDescendant(withText("Progress: 3")))))
    }

    @Test
    fun scrollRequiredToDisplayGoalFifty() {
        onView(withId(R.id.goal_recycler_view))
            .check(matches(not(atPosition(50, hasDescendant(withText("Goal #50"))))))
            .perform(scrollToPosition<GoalHolder>(50))
            .check(matches(atPosition(50, hasDescendant(withText("Goal #50")))))
    }

    @Test
    fun scrollToFiftyCantSeeFirstOrLast() {
        onView(withId(R.id.goal_recycler_view))
            .perform(scrollToPosition<GoalHolder>(50))
            .check(matches(not(atPosition(0, hasDescendant(withText("Goal #0"))))))
            .check(matches(not(atPosition(99, hasDescendant(withText("Goal #99"))))))
    }

    @Test
    fun lastFiveGoalsOnlyVisibleAfterScrollToBottom() {
        onView(withId(R.id.goal_recycler_view))
            .check(matches(not(atPosition(99, hasDescendant(withText("Goal #99"))))))
            .perform(scrollToPosition<GoalHolder>(99))
            .check(matches(atPosition(95, hasDescendant(withText("Goal #95")))))
            .check(matches(atPosition(96, hasDescendant(withText("Goal #96")))))
            .check(matches(atPosition(97, hasDescendant(withText("Goal #97")))))
            .check(matches(atPosition(98, hasDescendant(withText("Goal #98")))))
            .check(matches(atPosition(99, hasDescendant(withText("Goal #99")))))
    }

    @Test
    fun goalFourShowsNoNotesAndDeferredImage() {
        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(4, hasDescendant(withText("Goal #4")))))
            // .check(matches(atPosition(4, hasDescendant(withText("Progress: 0")))))
            .check(
                matches(
                    atPosition(
                        4,
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
    }

    @Test
    fun goalFiveShowsOneNoteAndCompletedImage() {
        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(5, hasDescendant(withText("Goal #5")))))
            .check(matches(atPosition(5, hasDescendant(withText("Progress: 1")))))
            .check(
                matches(
                    atPosition(
                        5,
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
    }

    @Test
    fun goalSixHasTwoNotesAndImageGone() {
        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(6, hasDescendant(withText("Goal #6")))))
            .check(matches(atPosition(6, hasDescendant(withText("Progress: 2")))))
            .check(
                matches(
                    atPosition(
                        6,
                        hasDescendant(
                            allOf(
                                withId(R.id.list_item_image),
                                withEffectiveVisibility(Visibility.GONE)
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun scrollToBottomThenTopRetainsProperImages() {
        onView(withId(R.id.goal_recycler_view))
            .perform(scrollToPosition<GoalHolder>(99))
            .perform(scrollToPosition<GoalHolder>(0))

            .check(matches(atPosition(4, hasDescendant(withText("Goal #4")))))
            .check(
                matches(
                    atPosition(
                        4,
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
            .check(matches(atPosition(5, hasDescendant(withText("Goal #5")))))
            .check(
                matches(
                    atPosition(
                        5,
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
    }


    @Test
    fun changeGoalSixToDeferredWithThreeNotes() {
        val createdScenario: FragmentScenario<GoalListFragment> = launchFragmentInContainer(
            initialState = Lifecycle.State.CREATED
        )

        createdScenario.withFragment {
            val dlfVM: GoalListViewModel by viewModels()
            val testGoal = dlfVM.goals[6].copy(
                title = "New Goal #6",
            ).apply {
                notes = dlfVM.goals[6].notes +
                        listOf(
                            GoalNote(
                                type = PROGRESS,
                                text = "Third Note",
                                goalId = id
                            ),
                            GoalNote(
                                type = PAUSED,
                                goalId = id
                            )
                        )
            }
            dlfVM.goals.set(6, testGoal)
        }

        createdScenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.goal_recycler_view))
            .check(matches(atPosition(6, hasDescendant(withText("New Goal #6")))))
            .check(matches(atPosition(6, hasDescendant(withText("Progress: 3")))))
            .check(
                matches(
                    atPosition(
                        6,
                        hasDescendant(
                            allOf(
                                withId(R.id.list_item_image),
                                matchesDrawable(R.drawable.ic_goal_paused)
                            )
                        )
                    )
                )
            )

        createdScenario.close()
    }

    // --- helper functions below ---

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
}