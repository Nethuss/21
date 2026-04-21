/** Поля в snake_case — как в ответе Spring (JdbcTemplate → Map). */
export interface ScheduleSlotDTO {
  id: number;
  group_id: number;
  subject_id: number;
  teacher_id: number;
  date: string;
  lesson_number: number;
  subject_name?: string | null;
  teacher_username?: string | null;
  teacher_firstname?: string | null;
  teacher_lastname?: string | null;
}

export interface ScheduleSlotWriteBody {
  group_id: number;
  subject_id: number;
  teacher_id: number;
  date: string;
  lesson_number: number;
}
