package benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.junit.internal.runners.InitializationError;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;

/*
 * Copyright (C) <2014> <Michele Bonazza>
 * 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
/**
 * A test runner that runs tests according to their position in the source file
 * of the test class.
 * 
 * @author Michele Bonazza
 */
public class OrderedTestRunner extends BlockJUnit4ClassRunner {
 
    /**
     * Creates a new runner
     * 
     * @param clazz
     *            the class being tested
     * @throws InitializationError
     *             if something goes wrong
     * @throws org.junit.runners.model.InitializationError 
     */
    public OrderedTestRunner(Class<?> clazz) throws  org.junit.runners.model.InitializationError {
        super(clazz);
    }
 
    /*
     * (non-Javadoc)
     * 
     * @see org.junit.runners.BlockJUnit4ClassRunner#computeTestMethods()
     */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        // get all methods to be tested
        List<FrameworkMethod> toSort = super.computeTestMethods();
 
        if (toSort.isEmpty())
            return toSort;
 
        // a map containing <line_number, method>
        final Map<Integer, FrameworkMethod> testMethods = new TreeMap<Integer, FrameworkMethod> ();
 
        // check that all methods here are declared in the same class, we don't
        // deal with test methods from superclasses that haven't been overridden
        Class<?> clazz = getDeclaringClass(toSort);
        if (clazz == null) {
            // fail explicitly
            System.err
                    .println("OrderedTestRunner can only run test classes that"
                            + " don't have test methods inherited from superclasses");
            return Collections.emptyList();
        }
 
        // use Javassist to figure out line numbers for methods
        ClassPool pool = ClassPool.getDefault();
        try {
            CtClass cc = pool.get(clazz.getName());
            // all methods in toSort are declared in the same class, we checked
            for (FrameworkMethod m : toSort) {
                String methodName = m.getName();
                CtMethod method = cc.getDeclaredMethod(methodName);
                testMethods.put(method.getMethodInfo().getLineNumber(0), m);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
 
        return new ArrayList<FrameworkMethod>(testMethods.values());
    }
 
    private Class<?> getDeclaringClass(List<FrameworkMethod> methods) {
        // methods can't be empty, it's been checked
        Class<?> clazz = methods.get(0).getMethod().getDeclaringClass();
 
        for (int i = 1; i < methods.size(); i++) {
            if (!methods.get(i).getMethod().getDeclaringClass().equals(clazz)) {
                // they must be all in the same class
                return null;
            }
        }
 
        return clazz;
    }
}