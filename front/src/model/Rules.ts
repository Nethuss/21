export const Rules = {
    required: (isRequired: boolean = true) => (value: string | null | undefined | boolean): boolean | string => {
      if (!isRequired) return true;
      if (typeof value === 'boolean') return true;
      return !!value || "Поле обязательно для заполнения"
    },
    maxLength: (length: number ) => (value: string) => {
        if (!value || value === "") return true;
        return (
            value?.length <= length || `Максимум ${length} символов`
        );
    },
  maxValue: (max: number) => (value: string) => {
    if (!value) return true;
    return (
      Number(value) < max || `Значение должно быть меньше ${max}`
    )
  },
  minValue: (min: number) => (value: string) => {
      if (!value) return true;
      return (
        Number(value) > min || `Значение должно быть больше ${min}`
      )
  },
    cadastralNumber: (value: string | null) => {
        if (!value || value === "") return true;
        return (
            /^\d{2}:\d{2}:\d{7}:\d{1,9}$/.test(value) ||
            "Кадастровый номер должен быть вида АА:ВВ:CCCCСCC:КК"
        );
    },
    isNumber: (value: string | number | null) => {
        if (value === null || value === "") return true;
        return (
            /^-?\d+(\.\d+)?$/.test(value.toString()) ||
            "Поле должно содержать только числа"
        );
    },
    maxArea: (value: number) =>
        value <= 10000 || `Слишком большое значение площади`,
    filesize: (files: File[]) => {
        const maxSize = 1024 * 1024; // Максимальный размер файла — 1MB
        for (const file of files) {
            if (file.size > maxSize) {
                return `Размер файла "${file.name}" слишком большой! Максимальный размер — 1MB`;
            }
        }
        return true;
    },
    time: (value: string | null) => {
        if (!value || value === "") return true;

        const timeParts = value.split(":");

        if (timeParts.length !== 2) return "Неверный формат времени. Используйте ЧЧ:ММ";
        if (timeParts[0].length !== 2 || timeParts[1].length !== 2) return "Неверный формат времени. Используйте ЧЧ:ММ.";

        const hours = parseInt(timeParts[0], 10);
        const minutes = parseInt(timeParts[1], 10);

        if (hours < 0 || hours > 23) {
            return "Часы должны быть в диапазоне от 00 до 23";
        }
        if (minutes < 0 || minutes > 59) {
            return "Минуты должны быть в диапазоне от 00 до 59";
        }
        return true;
    },
    latinOnly: (value: string | null | undefined) => {
        if (/[А-Яа-яЁё]/.test(value || "")) {
            return "Только латиница и специальные символы разрешены";
        } else return true

    },
    withoutSpaces: (value: string) => {
        if(value?.split(' ').length > 1) {
            return "Пробелы в логине не разрешены";
        } else return true
    },
    requiredIfVisible: (value: string | null | undefined, visible: boolean) => {
        return visible ? !!value || "Обязательное поле" : true;
    },
};

/**
 * Проверяет, является ли строковое значение корректным числом.
 *
 * Используется как правило валидации Vuetify. Пропускает пустые значения,
 * предполагая, что обязательность проверяется отдельно.
 *
 * @example
 * isNumber('123');   // true
 * isNumber('abc');   // 'Значение должно быть числом'
 * isNumber('');      // true
 */
export const isNumber = (value: string): true | string => {
  if (!value) return true;
  return !isNaN(Number(value)) || 'Значение должно быть числом';
};

/**
 * Создаёт правило валидации, проверяющее, что значение не превышает заданный максимум.
 *
 * Предполагается, что входное значение уже прошло проверку на число (например, через `isNumber`).
 * Пустые строки игнорируются.
 *
 * @param max — максимально допустимое значение
 * @param inclusive — включать ли границу (`<=` при `true`, `<` при `false`, по умолчанию `true`)
 *
 * @example
 * maxValue(100)('101') // 'Значение должно быть не больше 100'
 * maxValue(100, false)('100') // 'Значение должно быть меньше 100'
 */
export const maxValue = (max: number, inclusive = true) => (value: string): true | string => {
  if (!value) return true;
  const num = Number(value);
  if (isNaN(num)) return true;

  const isValid = inclusive ? num <= max : num < max;
  return isValid || `Значение должно быть ${inclusive ? 'не больше' : 'меньше'} ${max}`;
};

/**
 * Создаёт правило валидации, проверяющее, что значение не меньше заданного минимума.
 *
 * Аналогично `maxValue`, требует предварительной числовой валидации.
 *
 * @param min — минимально допустимое значение
 * @param inclusive — включать ли границу (`>=` при `true`, `>` при `false`, по умолчанию `true`)
 */
export const minValue = (min: number, inclusive = true) => (value: string): true | string => {
  if (!value) return true;
  const num = Number(value);
  if (isNaN(num)) return true;

  const isValid = inclusive ? num >= min : num > min;
  return isValid || `Значение должно быть ${inclusive ? 'не меньше' : 'больше'} ${min}`;
};

export const required = (isRequired: boolean = true) => (value: string): true | string => {
  if (!isRequired) return true;

  return !!value || "Поле обязательно для заполнения"
}
