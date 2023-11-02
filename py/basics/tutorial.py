import sys
import itertools
from dataclasses import dataclass


def strings():
    print(1 + 2, 1.0 + 2, 1 / 2, 1 // 2, 1**2, 1.0**2)
    print("abc"[-1], "abc"[-2:3], "abc"[0:2], "abc"[:2], "abc"[1:])
    print(("a" "b" "c") == "abc")
    print(str("abc"), "abc def 123".capitalize(), "AbCDeF".casefold())
    print(">>>", "123".center(6), "<<<")
    print("123\t456".expandtabs(2), "123".find("a"), "123".index("2"))
    print("int {0}, int again {0}, float {1}, str {2}".format(12, 12.3, "abc"))
    print(
        "int {the_int}, int again {the_int}, str {the_str}".format(
            **{"the_int": 12, "the_str": "hi"}
        )
    )
    test = ["ab", "ab12", "12", "12.3"]
    for thing in test:
        print(
            "thing {0}: digit? {1}, alnum? {2}, alpha? {3}, ascii? {4}, decimal? {5}, identifier? {6}, numeric? {7} ".format(
                thing,
                thing.isdigit(),
                thing.isalnum(),
                thing.isalpha(),
                thing.isascii(),
                thing.isdecimal(),
                thing.isidentifier(),
                thing.isnumeric(),
            )
        )


def lists():
    nested = [[1, 2], [3, 4]]
    print(sum(nested, []), sum([tuple(val) for val in nested], ()))
    if len(nested) > 1:
        print("nested len > 1")
    for v1, v2 in nested:
        print(f"tokens - v1 is {v1}, v2 is {v2}")
    for [v1, v2] in nested:
        print(f"list - v1 is {v1}, v2 is {v2}")
    for v1, v2 in nested:
        print(f"tuple - v1 is {v1}, v2 is {v2}")
    for i, val in enumerate(nested):
        print("index", i, "value", val)
    for i, val in enumerate([]):
        print("got a val - should not happen")
        break
    else:
        print("input is empty")
    nested.extend([[7, 8], [5, 6]])
    nested.sort(key=lambda elem: elem[1])
    print("extended and sorted", nested, "index", nested.index([5, 6]))
    del nested[:2]
    print(
        "after delete",
        nested,
        "test",
        [1, 2] in nested,
    )


def sets():
    nested = [[1, 2], [3, 4]]
    print(f"{nested} turned into a set: {set(itertools.chain(*nested))}")
    comp = set(sum(nested, []))
    print(f"turned into a set again: {comp}")


def matches():
    vals = [(1, 2), (3, 4), [4, 5]]
    for val in vals:
        match val:
            case (1, 2) | (3, 4):
                print("is a recognised tuple")
            case (x, y):
                print("other case", x, y)


def dicts():
    a: dict[str, dict[str, int]] = {"a": {"1": 2}}
    del a["a"]["1"]
    print("a is", a)
    for k, v in a.items():
        print(f"k is {k}, v is {v}")


def type_hinted_fun(in1: bool, in2: int) -> str:
    print(f"func annotations are {type_hinted_fun.__annotations__}")
    result = in1 + in2 + 1
    print(f"result is {result}")
    return "whatever"


def any_n_args(*more: bool):
    print(f"len: {len(more)}, more: {more}")


def more_args(pos1: str, pos2: int, *more: bool, **kwargs: int):
    print(f"pos1: {pos1}")
    print(f"pos2: {pos2}")
    print(f"more: {more}")
    print(f"kwargs: {kwargs}")
    print("string format dict key: {alpha}".format(**kwargs))


def more_kw_args(alpha=1, beta=2, charlie=3, delta=4):
    print(f"alpha: {alpha}, beta: {beta}, charlie: {charlie}, delta: {delta}")


def special_arg_params(alpha, beta, /, charlie, delta=1, *, foxtrot=2, golf):
    print(
        f"alpha: {alpha!a}, beta: {beta!r}, charlie: {charlie!s}, {delta=}, {foxtrot=}, {golf=}"
    )


def file_io():
    try:
        with open("does-not-exist", "rb") as rel:
            print("reading", rel.read())
    except (FileNotFoundError, Exception) as ex:
        print("file cannot be not found", ex)

    try:
        with open("/etc/os-release", "rb") as rel:
            content = rel.read().decode()[:10]
            print(f"content: {content}, fd position: {rel.tell()}")
            raise Exception("done reading")
    except Exception as ex:
        print("handled exception", ex)


@dataclass
class ThingName:
    name: str


class Thing:
    name: ThingName = ThingName(name="Alpha")

    def get_name(self):
        return f"name is {self.name}"


def class_fun():
    first = Thing()
    print("class function return value:", first.get_name())
    Thing.name.name = "Beta"
    second = Thing()
    print(f"both attribute values: {first.name.name} {second.name.name}")
    second.name.name = "Charlie"
    print(f"both attribute values: {first.name.name} {second.name.name}")


def main():
    print(
        "starting up, args are:\n",
        sys.argv,
        "\npaths are:\n",
        sys.path,
        "\nnumber of modules:\t",
        len(sys.modules),
        "\nname is:\t",
        __name__,
    )

    # strings()
    # lists()
    # sets()
    # matches()
    # dicts()
    # type_hinted_fun(111, 222)
    # any_n_args(True, False)
    # any_n_args(*(True, False))
    # any_n_args(*[True, False], *(False, True), True)
    # more_args("pos111", 222, True, False, alpha=1, beta=2)
    # more_args("pos111", 222, *(True, False), **{"alpha": 1, "beta": 2})
    # more_kw_args(**{'alpha': 1, 'beta': 2}, charlie=333, **{'delta':444})
    # special_arg_params(111, 222, 333, golf=444)
    # special_arg_params(111, 222, *(333, 444), **{"foxtrot": 555}, golf=6)
    # file_io()
    class_fun()
