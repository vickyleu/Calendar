package io.wojciechosak.calendar.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.wojciechosak.calendar.animation.CalendarAnimator
import io.wojciechosak.calendar.config.CalendarConfig
import io.wojciechosak.calendar.config.CalendarConstants.INITIAL_PAGE_INDEX
import io.wojciechosak.calendar.config.CalendarConstants.MAX_PAGES
import io.wojciechosak.calendar.config.MonthYear
import io.wojciechosak.calendar.config.rememberCalendarState
import io.wojciechosak.calendar.utils.toMonthYear
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * Composable function to display a horizontal calendar view.
 *
 * @param startDate The start date of the calendar.
 * @param pagerState The PagerState used to control the horizontal paging behavior of the calendar.
 * @param modifier The modifier for styling and layout of the calendar.
 * @param pageSize The size of each page in the calendar. Default is [PageSize.Fill].
 * @param beyondBoundsPageCount The number of pages to keep loaded beyond the visible bounds. Default is 0.
 * @param contentPadding The padding applied around the content of the day cell.
 * @param calendarAnimator The animator used for animating calendar transitions.
 * @param calendarView The composable function to display the content of each calendar page.
 */
@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalCalendarView(
	startDate: LocalDate,
	pagerState: PagerState = rememberPagerState(
		initialPage = INITIAL_PAGE_INDEX,
		pageCount = { MAX_PAGES },
	),
	config: MutableState<CalendarConfig> = rememberCalendarState(
		startDate = startDate,
		monthOffset = 0,
	),
	modifier: Modifier = Modifier,
	pageSize: PageSize = PageSize.Fill,
//    beyondBoundsPageCount: Int = 0,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	header: @Composable (month: Month, year: Int,calendarAnimator: CalendarAnimator) -> Unit = { month, year,calendarAnimator ->
		MonthHeader(month, year)
	},
	yearMonth: MutableState<MonthYear> = remember { mutableStateOf(config.value.monthYear) },
	calendarAnimator: CalendarAnimator = CalendarAnimator(startDate),
	calendarView: @Composable (
		monthOffset: Int, config: MutableState<CalendarConfig>,
		header: @Composable (month: Month, year: Int,calendarAnimator:CalendarAnimator) -> Unit
	) -> Unit = { monthOffset, config, header ->
		CalendarView(
			day = { dayState ->
				CalendarDay(
					state = dayState,
					onClick = { },
				)
			},
			header = header,
			yearMonth = yearMonth,
			config = config,
		)
	},
) {
	Column(modifier = modifier) {
		if (!config.value.headerCanScroll && config.value.showHeader) {
			Box(modifier = Modifier) {
				header(yearMonth.value.month, yearMonth.value.year,calendarAnimator)
			}
		}

		LaunchedEffect(Unit) {
			snapshotFlow { pagerState.currentPage }
				.distinctUntilChanged()
				.collect{
					val index = it - INITIAL_PAGE_INDEX

					config.value = config.value.copy(
						monthYear = startDate.plus(index, DateTimeUnit.MONTH).toMonthYear()
					)
					println("monthOffset::index:::$index " +
						"  startDate:${startDate.monthNumber}" +
						" ${startDate.plus(index, DateTimeUnit.MONTH).toMonthYear().month.number}")
					yearMonth.value = config.value.monthYear
				}
		}

		HorizontalPager(
			state = pagerState,
			pageSize = pageSize,
			verticalAlignment = Alignment.Top,
//    TODO 缺失    beyondBoundsPageCount = beyondBoundsPageCount,
			contentPadding = contentPadding,
		) {
			val index = it - INITIAL_PAGE_INDEX
			calendarAnimator.updatePagerState(pagerState)
			LaunchedEffect(Unit) {
				calendarAnimator.setAnimationMode(CalendarAnimator.AnimationMode.MONTH)
			}


			Column {
				println("monthOffset::${index}")
				calendarView(index, config, header,)
			}
		}
	}

}
