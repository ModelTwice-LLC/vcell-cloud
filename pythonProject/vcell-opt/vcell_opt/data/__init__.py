# Code generated by jtd-codegen for Python v0.3.1

import re
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from enum import Enum
from typing import Any, Dict, List, Optional, Union, get_args, get_origin


class VcelloptStatus(Enum):
    COMPLETE = "complete"
    FAILED = "failed"
    QUEUED = "queued"
    RUNNING = "running"
    @classmethod
    def from_json_data(cls, data: Any) -> 'VcelloptStatus':
        return cls(data)

    def to_json_data(self) -> Any:
        return self.value

@dataclass
class Vcellopt:
    opt_problem: 'OptProblem'
    opt_result_set: 'OptResultSet'
    status: 'VcelloptStatus'
    status_message: 'str'

    @classmethod
    def from_json_data(cls, data: Any) -> 'Vcellopt':
        return cls(
            _from_json_data(OptProblem, data.get("optProblem")),
            _from_json_data(OptResultSet, data.get("optResultSet")),
            _from_json_data(VcelloptStatus, data.get("status")),
            _from_json_data(str, data.get("statusMessage")),
        )

    def to_json_data(self) -> Any:
        data: Dict[str, Any] = {}
        data["optProblem"] = _to_json_data(self.opt_problem)
        data["optResultSet"] = _to_json_data(self.opt_result_set)
        data["status"] = _to_json_data(self.status)
        data["statusMessage"] = _to_json_data(self.status_message)
        return data

class CopasiOptimizationMethodOptimizationMethodType(Enum):
    SRES = "SRES"
    EVOLUTIONARY_PROGRAM = "evolutionaryProgram"
    GENETIC_ALGORITHM = "geneticAlgorithm"
    GENETIC_ALGORITHM_SR = "geneticAlgorithmSR"
    HOOKE_JEEVES = "hookeJeeves"
    LEVENBERG_MARQUARDT = "levenbergMarquardt"
    NELDER_MEAD = "nelderMead"
    PARTICLE_SWARM = "particleSwarm"
    PRAXIS = "praxis"
    RANDOM_SEARCH = "randomSearch"
    SIMULATED_ANNEALING = "simulatedAnnealing"
    STEEPEST_DESCENT = "steepestDescent"
    TRUNCATED_NEWTON = "truncatedNewton"
    @classmethod
    def from_json_data(cls, data: Any) -> 'CopasiOptimizationMethodOptimizationMethodType':
        return cls(data)

    def to_json_data(self) -> Any:
        return self.value

@dataclass
class CopasiOptimizationMethod:
    optimization_method_type: 'CopasiOptimizationMethodOptimizationMethodType'
    optimization_parameter: 'List[CopasiOptimizationParameter]'

    @classmethod
    def from_json_data(cls, data: Any) -> 'CopasiOptimizationMethod':
        return cls(
            _from_json_data(CopasiOptimizationMethodOptimizationMethodType, data.get("optimizationMethodType")),
            _from_json_data(List[CopasiOptimizationParameter], data.get("optimizationParameter")),
        )

    def to_json_data(self) -> Any:
        data: Dict[str, Any] = {}
        data["optimizationMethodType"] = _to_json_data(self.optimization_method_type)
        data["optimizationParameter"] = _to_json_data(self.optimization_parameter)
        return data

class CopasiOptimizationParameterDataType(Enum):
    DOUBLE = "double"
    INT = "int"
    @classmethod
    def from_json_data(cls, data: Any) -> 'CopasiOptimizationParameterDataType':
        return cls(data)

    def to_json_data(self) -> Any:
        return self.value

class CopasiOptimizationParameterParamType(Enum):
    COOLING_FACTOR = "coolingFactor"
    ITERATION_LIMIT = "iterationLimit"
    NUMBER_OF_GENERATIONS = "numberOfGenerations"
    NUMBER_OF_ITERATIONS = "numberOfIterations"
    PF = "pf"
    POPULATION_SIZE = "populationSize"
    RANDOM_NUMBER_GENERATOR = "randomNumberGenerator"
    RHO = "rho"
    SCALE = "scale"
    SEED = "seed"
    START_TEMPERATURE = "startTemperature"
    STD_DEVIATION = "stdDeviation"
    SWARM_SIZE = "swarmSize"
    TOLERANCE = "tolerance"
    @classmethod
    def from_json_data(cls, data: Any) -> 'CopasiOptimizationParameterParamType':
        return cls(data)

    def to_json_data(self) -> Any:
        return self.value

@dataclass
class CopasiOptimizationParameter:
    data_type: 'CopasiOptimizationParameterDataType'
    param_type: 'CopasiOptimizationParameterParamType'
    value: 'float'

    @classmethod
    def from_json_data(cls, data: Any) -> 'CopasiOptimizationParameter':
        return cls(
            _from_json_data(CopasiOptimizationParameterDataType, data.get("dataType")),
            _from_json_data(CopasiOptimizationParameterParamType, data.get("paramType")),
            _from_json_data(float, data.get("value")),
        )

    def to_json_data(self) -> Any:
        data: Dict[str, Any] = {}
        data["dataType"] = _to_json_data(self.data_type)
        data["paramType"] = _to_json_data(self.param_type)
        data["value"] = _to_json_data(self.value)
        return data

@dataclass
class OptProblem:
    copasi_optimization_method: 'CopasiOptimizationMethod'
    data_set: 'List[List[float]]'
    math_model_sbml_contents: 'str'
    number_of_optimization_runs: 'int'
    parameter_description_list: 'List[ParameterDescription]'
    reference_variable: 'List[ReferenceVariable]'

    @classmethod
    def from_json_data(cls, data: Any) -> 'OptProblem':
        return cls(
            _from_json_data(CopasiOptimizationMethod, data.get("copasiOptimizationMethod")),
            _from_json_data(List[List[float]], data.get("dataSet")),
            _from_json_data(str, data.get("mathModelSbmlContents")),
            _from_json_data(int, data.get("numberOfOptimizationRuns")),
            _from_json_data(List[ParameterDescription], data.get("parameterDescriptionList")),
            _from_json_data(List[ReferenceVariable], data.get("referenceVariable")),
        )

    def to_json_data(self) -> Any:
        data: Dict[str, Any] = {}
        data["copasiOptimizationMethod"] = _to_json_data(self.copasi_optimization_method)
        data["dataSet"] = _to_json_data(self.data_set)
        data["mathModelSbmlContents"] = _to_json_data(self.math_model_sbml_contents)
        data["numberOfOptimizationRuns"] = _to_json_data(self.number_of_optimization_runs)
        data["parameterDescriptionList"] = _to_json_data(self.parameter_description_list)
        data["referenceVariable"] = _to_json_data(self.reference_variable)
        return data

@dataclass
class OptResultSet:
    num_function_evaluations: 'int'
    objective_function: 'float'
    opt_parameter_values: 'Dict[str, float]'

    @classmethod
    def from_json_data(cls, data: Any) -> 'OptResultSet':
        return cls(
            _from_json_data(int, data.get("numFunctionEvaluations")),
            _from_json_data(float, data.get("objectiveFunction")),
            _from_json_data(Dict[str, float], data.get("optParameterValues")),
        )

    def to_json_data(self) -> Any:
        data: Dict[str, Any] = {}
        data["numFunctionEvaluations"] = _to_json_data(self.num_function_evaluations)
        data["objectiveFunction"] = _to_json_data(self.objective_function)
        data["optParameterValues"] = _to_json_data(self.opt_parameter_values)
        return data

@dataclass
class ParameterDescription:
    initial_value: 'float'
    max_value: 'float'
    min_value: 'float'
    name: 'str'
    scale: 'float'

    @classmethod
    def from_json_data(cls, data: Any) -> 'ParameterDescription':
        return cls(
            _from_json_data(float, data.get("initialValue")),
            _from_json_data(float, data.get("maxValue")),
            _from_json_data(float, data.get("minValue")),
            _from_json_data(str, data.get("name")),
            _from_json_data(float, data.get("scale")),
        )

    def to_json_data(self) -> Any:
        data: Dict[str, Any] = {}
        data["initialValue"] = _to_json_data(self.initial_value)
        data["maxValue"] = _to_json_data(self.max_value)
        data["minValue"] = _to_json_data(self.min_value)
        data["name"] = _to_json_data(self.name)
        data["scale"] = _to_json_data(self.scale)
        return data

class ReferenceVariableReferenceVariableType(Enum):
    DEPENDENT = "dependent"
    INDEPENDENT = "independent"
    @classmethod
    def from_json_data(cls, data: Any) -> 'ReferenceVariableReferenceVariableType':
        return cls(data)

    def to_json_data(self) -> Any:
        return self.value

@dataclass
class ReferenceVariable:
    reference_variable_type: 'ReferenceVariableReferenceVariableType'
    var_name: 'str'

    @classmethod
    def from_json_data(cls, data: Any) -> 'ReferenceVariable':
        return cls(
            _from_json_data(ReferenceVariableReferenceVariableType, data.get("referenceVariableType")),
            _from_json_data(str, data.get("varName")),
        )

    def to_json_data(self) -> Any:
        data: Dict[str, Any] = {}
        data["referenceVariableType"] = _to_json_data(self.reference_variable_type)
        data["varName"] = _to_json_data(self.var_name)
        return data

def _from_json_data(cls: Any, data: Any) -> Any:
    if data is None or cls in [bool, int, float, str, object] or cls is Any:
        return data
    if cls is datetime:
        return _parse_rfc3339(data)
    if get_origin(cls) is Union:
        return _from_json_data(get_args(cls)[0], data)
    if get_origin(cls) is list:
        return [_from_json_data(get_args(cls)[0], d) for d in data]
    if get_origin(cls) is dict:
        return { k: _from_json_data(get_args(cls)[1], v) for k, v in data.items() }
    return cls.from_json_data(data)

def _to_json_data(data: Any) -> Any:
    if data is None or type(data) in [bool, int, float, str, object]:
        return data
    if type(data) is datetime:
        return data.isoformat()
    if type(data) is list:
        return [_to_json_data(d) for d in data]
    if type(data) is dict:
        return { k: _to_json_data(v) for k, v in data.items() }
    return data.to_json_data()

def _parse_rfc3339(s: str) -> datetime:
    datetime_re = '^(\d{4})-(\d{2})-(\d{2})[tT](\d{2}):(\d{2}):(\d{2})(\.\d+)?([zZ]|((\+|-)(\d{2}):(\d{2})))$'
    match = re.match(datetime_re, s)
    if not match:
        raise ValueError('Invalid RFC3339 date/time', s)

    (year, month, day, hour, minute, second, frac_seconds, offset,
     *tz) = match.groups()

    frac_seconds_parsed = None
    if frac_seconds:
        frac_seconds_parsed = int(float(frac_seconds) * 1_000_000)
    else:
        frac_seconds_parsed = 0

    tzinfo = None
    if offset == 'Z':
        tzinfo = timezone.utc
    else:
        hours = int(tz[2])
        minutes = int(tz[3])
        sign = 1 if tz[1] == '+' else -1

        if minutes not in range(60):
            raise ValueError('minute offset must be in 0..59')

        tzinfo = timezone(timedelta(minutes=sign * (60 * hours + minutes)))

    second_parsed = int(second)
    if second_parsed == 60:
        second_parsed = 59

    return datetime(int(year), int(month), int(day), int(hour), int(minute),
                    second_parsed, frac_seconds_parsed, tzinfo)            
