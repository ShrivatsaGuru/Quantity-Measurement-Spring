// Measurement categories, their units, and their Spring measurementType string
export const CATEGORIES = [
  {
    key: 'length',
    label: 'Length',
    type: 'LengthUnit',
    icon: '📏',
    units: ['FEET', 'INCHES', 'YARDS', 'CENTIMETERS', 'METERS', 'KILOMETERS', 'MILES'],
    defaultUnit: 'FEET',
  },
  {
    key: 'weight',
    label: 'Weight',
    type: 'WeightUnit',
    icon: '⚖️',
    units: ['GRAM', 'KILOGRAM', 'MILLIGRAM', 'POUND', 'TONNE'],
    defaultUnit: 'KILOGRAM',
  },
  {
    key: 'temperature',
    label: 'Temperature',
    type: 'TemperatureUnit',
    icon: '🌡️',
    units: ['CELSIUS', 'FAHRENHEIT', 'KELVIN'],
    defaultUnit: 'CELSIUS',
  },
  {
    key: 'volume',
    label: 'Volume',
    type: 'VolumeUnit',
    icon: '🧪',
    units: ['LITRE', 'MILLILITER', 'GALLON', 'CUBIC_METER'],
    defaultUnit: 'LITRE',
  },
]

export const ACTIONS = [
  { key: 'compare',    label: 'Comparison' },
  { key: 'convert',   label: 'Conversion' },
  { key: 'arithmetic', label: 'Arithmetic' },
]

export const OPERATORS = [
  { key: 'add',      symbol: '+', label: 'Add' },
  { key: 'subtract', symbol: '−', label: 'Subtract' },
  { key: 'multiply', symbol: '×', label: 'Multiply' },
  { key: 'divide',   symbol: '÷', label: 'Divide' },
]

// Build a QuantityDTO for API requests
export const qty = (value, unit, measurementType) => ({ value: Number(value), unit, measurementType })
