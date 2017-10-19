/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.transaction.core.service.impl;

import com.happylifeplat.transaction.core.bean.TransactionInvocation;
import com.happylifeplat.transaction.core.bean.TxTransactionInfo;
import com.happylifeplat.transaction.core.concurrent.threadlocal.CompensationLocal;
import com.happylifeplat.transaction.core.helper.SpringBeanUtils;
import com.happylifeplat.transaction.core.service.AspectTransactionService;
import com.happylifeplat.transaction.core.service.TxTransactionFactoryService;
import com.happylifeplat.transaction.core.service.TxTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * @author xiaoyu
 */
@Service
public class AspectTransactionServiceImpl implements AspectTransactionService {

    private final TxTransactionFactoryService txTransactionFactoryService;

    @Autowired
    public AspectTransactionServiceImpl(TxTransactionFactoryService txTransactionFactoryService) {
        this.txTransactionFactoryService = txTransactionFactoryService;
    }

    /**
     *
     * ProceedingJoinPoint(连接点对象)中的proceed()方法来执行目标方法（即被环绕通知包围的方法）
     */
    @Override
    public Object invoke(String transactionGroupId, ProceedingJoinPoint point) throws Throwable {
        //方法签名 MethodSignature可以获取Method和returnType
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Class<?> clazz = point.getTarget().getClass();
        Object[] args = point.getArgs();

        Method thisMethod = clazz.getMethod(method.getName(), method.getParameterTypes());

        // TODO: 2017/10/16  
        final String compensationId = CompensationLocal.getInstance().getCompensationId();

        TransactionInvocation invocation = new TransactionInvocation(clazz, thisMethod.getName(), args, method.getParameterTypes());
        TxTransactionInfo info = new TxTransactionInfo(compensationId, transactionGroupId, invocation);
        final Class c = txTransactionFactoryService.factoryOf(info);
        final TxTransactionHandler txTransactionHandler =
                (TxTransactionHandler) SpringBeanUtils.getInstance().getBean(c);
        return txTransactionHandler.handler(point, info);
    }
}
