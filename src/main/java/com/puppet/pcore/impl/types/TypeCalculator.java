package com.puppet.pcore.impl.types;

import com.puppet.pcore.Binary;
import com.puppet.pcore.Default;
import com.puppet.pcore.PObject;
import com.puppet.pcore.Pcore;
import com.puppet.pcore.impl.Polymorphic;
import com.puppet.pcore.semver.Version;
import com.puppet.pcore.semver.VersionRange;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.puppet.pcore.impl.types.TypeFactory.*;

@SuppressWarnings("unused")
class TypeCalculator extends Polymorphic<AnyType> {

	static final TypeCalculator SINGLETON = new TypeCalculator();
	private static final DispatchMap dispatchMap = initPolymorphicDispatch(TypeCalculator.class, "_infer");

	private TypeCalculator() {
	}

	@Override
	protected DispatchMap getDispatchMap() {
		return dispatchMap;
	}

	AnyType _infer(AnyType o) {
		return typeType(o);
	}

	AnyType _infer(Binary o) {
		return binaryType();
	}

	AnyType _infer(Boolean o) {
		return booleanType();
	}

	AnyType _infer(Byte o) {
		long val = o.longValue();
		return integerType(val, val);
	}

	AnyType _infer(Collection<?> o) {
		return collectionType(sizeAsType(o));
	}

	AnyType _infer(Default o) {
		return defaultType();
	}

	AnyType _infer(Duration o) {
		return timeSpanType(o, o);
	}

	AnyType _infer(Double o) {
		return floatType(o, o);
	}

	AnyType _infer(Float o) {
		double val = o.doubleValue();
		return floatType(val, val);
	}

	AnyType _infer(Instant o) {
		return timestampType(o, o);
	}

	AnyType _infer(Integer o) {
		long val = o.longValue();
		return integerType(val, val);
	}

	AnyType _infer(List<?> o) {
		return o.isEmpty() ? ArrayType.EMPTY : arrayType(inferAndReduceType(o), sizeAsType(o));
	}

	AnyType _infer(Long o) {
		return integerType(o, o);
	}

	AnyType _infer(Map<?,?> o) {
		return o.isEmpty()
				? HashType.EMPTY
				: hashType(inferAndReduceType(o.keySet()), inferAndReduceType(o.values()), sizeAsType(o));
	}

	AnyType _infer(Object o) {
		Pcore pcore = Pcore.INSTANCE;
		AnyType type = (AnyType)pcore.implementationRegistry().typeFor(o.getClass(), pcore.typeEvaluator());
		return type == null ? runtimeType("java", o.getClass().getName()) : type;
	}

	AnyType _infer(Pattern o) {
		return regexpType(o);
	}

	AnyType _infer(PObject o) {
		return (AnyType)o._pType();
	}

	AnyType _infer(Short o) {
		long val = o.longValue();
		return integerType(val, val);
	}

	AnyType _infer(String o) {
		return stringType(o);
	}

	AnyType _infer(Version o) {
		return semVerType(VersionRange.exact(o));
	}

	AnyType _infer(VersionRange o) {
		return semVerRangeType();
	}

	AnyType _infer(Void o) {
		return undefType();
	}

	AnyType infer(Object o) {
		try {
			return dispatch(o);
		} catch(InvocationTargetException e) {
			Throwable te = e.getCause();
			if(!(te instanceof RuntimeException))
				te = new RuntimeException(te);
			throw (RuntimeException)te;
		}
	}

	AnyType inferAndReduceType(Collection<?> objects) {
		return reduceType(objects.stream().map(this::infer));
	}

	AnyType inferSet(Object o) {
		if(o instanceof Collection<?>) {
			Collection<?> cv = (Collection<?>)o;
			return cv.isEmpty() ? ArrayType.EMPTY : tupleType(cv.stream().map(this::inferSet));
		}
		if(o instanceof Map<?,?>) {
			Map<?,?> ho = (Map<?,?>)o;
			if(ho.keySet().stream().allMatch(StringType.NOT_EMPTY::isInstance))
				return structType(ho.entrySet().stream().map(e -> new StructElement(stringType((String)e.getKey()), inferSet(e.getValue()))));

			AnyType keyType = variantType(ho.keySet().stream().map(this::inferSet));
			AnyType valueType = variantType(ho.values().stream().map(this::inferSet));
			return hashType(keyType, valueType, sizeAsType(ho));
		}
		return infer(o);
	}

	AnyType reduceType(Stream<AnyType> types) {
		return types.reduce(unitType(), AnyType::common);
	}

	private IntegerType sizeAsType(Collection<?> c) {
		long sz = c.size();
		return integerType(sz, sz);
	}

	private IntegerType sizeAsType(Map<?,?> c) {
		long sz = c.size();
		return integerType(sz, sz);
	}
}