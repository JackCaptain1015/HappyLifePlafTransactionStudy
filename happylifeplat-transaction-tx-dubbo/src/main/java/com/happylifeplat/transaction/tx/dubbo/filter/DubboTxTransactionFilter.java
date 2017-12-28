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
package com.happylifeplat.transaction.tx.dubbo.filter;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.happylifeplat.transaction.common.constant.CommonConstant;
import com.happylifeplat.transaction.core.concurrent.threadlocal.TxTransactionLocal;

/***
 * 有需要自己实现dubbo过滤器的，可关注如下步骤：
 dubbo初始化过程加载META-INF/dubbo/internal/，META-INF/dubbo/，
 META-INF/services/三个路径(classloaderresource)下面的com.alibaba.dubbo.rpc.Filter文件
 文件配置每行Name=FullClassName，必须是实现Filter接口

 @Activate标注扩展能被自动激活
 @Activate如果group（provider|consumer）匹配才被加载
 @Activate的value字段标明过滤条件，不写则所有条件下都会被加载，写了则只有dubbo URL中包含该参数名且参数值不为空才被加载
 如下是dubbo rpc access log的过滤器，仅对服务提供方有效，
 且参数中需要带accesslog，也就是配置protocol或者serivce时配置的accesslog="d:/rpc_access.log"
 */
//这是一个dubbo过滤器
@Activate(group = {Constants.SERVER_KEY, Constants.CONSUMER})
public class DubboTxTransactionFilter implements Filter {

    /***
     * 为consumer设置同一个tx-group Id
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (RpcContext.getContext().isConsumerSide()) {
            RpcContext.getContext().setAttachment(CommonConstant.TX_TRANSACTION_GROUP,
                    TxTransactionLocal.getInstance().getTxGroupId());
        }
        return invoker.invoke(invocation);
    }
}
