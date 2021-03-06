/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance2.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.performance;

import java.util.Calendar;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;

class Worker4 extends Thread
{

    public Worker4(int iters)
    {
        _iters = iters;
    }

    public void run()
    {
        for (int i = 0; i < _iters; i++) {
            try {
                AtomicAction A = new AtomicAction();

                A.begin();

                A.add(new BasicRecord());
                A.add(new BasicRecord());
                
                A.commit();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        Performance4.doSignal();
    }

    private int _iters;

}


public class Performance4
{
    public static void main (String[] args)
    {
        Performance4 obj = new Performance4();
        
        obj.setWorkSize(1000);
        
        obj.test();
    }
    
    public void setWorkSize (int size)
    {
        _sizeOfWork = size;
    }
    
    @Test
    public void test()
    {
        int threads = 10;
        int work = _sizeOfWork;

        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(TwoPhaseVolatileStore.class.getName());
        
        number = threads;

        int numberOfTransactions = threads * work;
        long stime = Calendar.getInstance().getTime().getTime();
        Worker4[] workers = new Worker4[threads];

        for (int i = 0; i < threads; i++) {
            workers[i] = new Worker4(work);

            workers[i].start();
        }

        Performance4.doWait();

        long ftime = Calendar.getInstance().getTime().getTime();
        long timeTaken = ftime - stime;

        System.out.println("time for " + numberOfTransactions + " write transactions is " + timeTaken);
        System.out.println("number of transactions: " + numberOfTransactions);
        System.out.println("throughput: " + (float) (numberOfTransactions / (timeTaken / 1000.0)));
    }

    public static void doWait()
    {
        try {
            synchronized (sync) {
                if (number > 0)
                    sync.wait();
            }
        }
        catch (Exception e) {
        }
    }

    public static void doSignal()
    {
        synchronized (sync) {
            if (--number == 0)
                sync.notify();
        }
    }

    int _sizeOfWork = 100;
    
    private static Object sync = new Object();
    private static int number = 0;

}
