import java.time.{LocalDate, Period}

val start =LocalDate.of(2015,7,13)
val end = LocalDate.of(2049,12,31)

start.plus(Period.of(34, 0, 171)).plusDays(1)