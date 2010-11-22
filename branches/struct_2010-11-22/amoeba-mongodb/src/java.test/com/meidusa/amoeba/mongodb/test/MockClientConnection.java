/*
 * Copyright amoeba.meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mongodb.test;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import org.bson.BSONObject;
import org.bson.JSON;

import com.meidusa.amoeba.mongodb.net.AbstractMongodbConnection;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;

/**
 * 
 * @author struct
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class MockClientConnection extends AbstractMongodbConnection{
	static String string = "{'list':[{ '_id' : { '$oid' : '4c9e1d302d159a51896ae0ae'} , 'sdid' : 4869 , 'fsdid' : 391755 , 'name' : 'struct211' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d312d159a51896ae8ec'} , 'sdid' : 4869 , 'fsdid' : 694553 , 'name' : 'struct113' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d312d159a51896aed2a'} , 'sdid' : 4869 , 'fsdid' : 190838 , 'name' : 'struct324' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d322d159a51896af70a'} , 'sdid' : 4869 , 'fsdid' : 801422 , 'name' : 'struct40' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d322d159a51896afcbf'} , 'sdid' : 4869 , 'fsdid' : 867232 , 'name' : 'struct176' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d332d159a51896b118d'} , 'sdid' : 4869 , 'fsdid' : 811386 , 'name' : 'struct332' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d342d159a51896b1b24'} , 'sdid' : 4869 , 'fsdid' : 35874 , 'name' : 'struct137' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d342d159a51896b1c12'} , 'sdid' : 4869 , 'fsdid' : 827797 , 'name' : 'struct472' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d352d159a51896b2c20'} , 'sdid' : 4869 , 'fsdid' : 853944 , 'name' : 'struct476' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d352d159a51896b2e33'} , 'sdid' : 4869 , 'fsdid' : 925067 , 'name' : 'struct125' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d352d159a51896b2e5e'} , 'sdid' : 4869 , 'fsdid' : 890310 , 'name' : 'struct128' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d362d159a51896b3526'} , 'sdid' : 4869 , 'fsdid' : 123242 , 'name' : 'struct438' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d362d159a51896b3527'} , 'sdid' : 4869 , 'fsdid' : 147279 , 'name' : 'struct146' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d362d159a51896b3f8c'} , 'sdid' : 4869 , 'fsdid' : 155854 , 'name' : 'struct344' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d372d159a51896b4d38'} , 'sdid' : 4869 , 'fsdid' : 636792 , 'name' : 'struct18' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d372d159a51896b510b'} , 'sdid' : 4869 , 'fsdid' : 523334 , 'name' : 'struct376' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d382d159a51896b5872'} , 'sdid' : 4869 , 'fsdid' : 105711 , 'name' : 'struct320' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3b2d159a51896b965b'} , 'sdid' : 4869 , 'fsdid' : 197641 , 'name' : 'struct440' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3c2d159a51896b9afe'} , 'sdid' : 4869 , 'fsdid' : 616397 , 'name' : 'struct15' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3c2d159a51896baa1d'} , 'sdid' : 4869 , 'fsdid' : 146869 , 'name' : 'struct360' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3d2d159a51896bb6e4'} , 'sdid' : 4869 , 'fsdid' : 899587 , 'name' : 'struct390' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3e2d159a51896bc46e'} , 'sdid' : 4869 , 'fsdid' : 447789 , 'name' : 'struct189' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3e2d159a51896bc5fc'} , 'sdid' : 4869 , 'fsdid' : 191382 , 'name' : 'struct137' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3e2d159a51896bcae8'} , 'sdid' : 4869 , 'fsdid' : 473458 , 'name' : 'struct120' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d3f2d159a51896bcd6c'} , 'sdid' : 4869 , 'fsdid' : 954434 , 'name' : 'struct478' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d412d159a51896bf5c8'} , 'sdid' : 4869 , 'fsdid' : 1665 , 'name' : 'struct372' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d412d159a51896bfb2c'} , 'sdid' : 4869 , 'fsdid' : 222148 , 'name' : 'struct488' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d412d159a51896bfc2d'} , 'sdid' : 4869 , 'fsdid' : 952794 , 'name' : 'struct310' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d412d159a51896bfe35'} , 'sdid' : 4869 , 'fsdid' : 41506 , 'name' : 'struct478' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d412d159a51896bfe71'} , 'sdid' : 4869 , 'fsdid' : 400894 , 'name' : 'struct266' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d422d159a51896c0c5b'} , 'sdid' : 4869 , 'fsdid' : 178513 , 'name' : 'struct254' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d432d159a51896c1172'} , 'sdid' : 4869 , 'fsdid' : 995655 , 'name' : 'struct72' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d432d159a51896c1f61'} , 'sdid' : 4869 , 'fsdid' : 925717 , 'name' : 'struct61' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d442d159a51896c2795'} , 'sdid' : 4869 , 'fsdid' : 998426 , 'name' : 'struct122' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d472d159a51896c5914'} , 'sdid' : 4869 , 'fsdid' : 801843 , 'name' : 'struct40' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d472d159a51896c59f0'} , 'sdid' : 4869 , 'fsdid' : 178813 , 'name' : 'struct151' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d472d159a51896c5af2'} , 'sdid' : 4869 , 'fsdid' : 247230 , 'name' : 'struct434' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d472d159a51896c5f5f'} , 'sdid' : 4869 , 'fsdid' : 272173 , 'name' : 'struct392' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d482d159a51896c71eb'} , 'sdid' : 4869 , 'fsdid' : 322290 , 'name' : 'struct149' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d482d159a51896c7314'} , 'sdid' : 4869 , 'fsdid' : 818628 , 'name' : 'struct246' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d482d159a51896c7317'} , 'sdid' : 4869 , 'fsdid' : 211832 , 'name' : 'struct269' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d482d159a51896c73c0'} , 'sdid' : 4869 , 'fsdid' : 949811 , 'name' : 'struct257' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d482d159a51896c76de'} , 'sdid' : 4869 , 'fsdid' : 91255 , 'name' : 'struct258' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d492d159a51896c84af'} , 'sdid' : 4869 , 'fsdid' : 803831 , 'name' : 'struct365' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d492d159a51896c886f'} , 'sdid' : 4869 , 'fsdid' : 796304 , 'name' : 'struct458' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d492d159a51896c88ea'} , 'sdid' : 4869 , 'fsdid' : 957766 , 'name' : 'struct477' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4a2d159a51896c8afb'} , 'sdid' : 4869 , 'fsdid' : 7534 , 'name' : 'struct190' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4a2d159a51896c8e05'} , 'sdid' : 4869 , 'fsdid' : 566882 , 'name' : 'struct302' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4b2d159a51896ca690'} , 'sdid' : 4869 , 'fsdid' : 409504 , 'name' : 'struct345' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4e2d159a51896cd288'} , 'sdid' : 4869 , 'fsdid' : 412625 , 'name' : 'struct168' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4e2d159a51896cd2ce'} , 'sdid' : 4869 , 'fsdid' : 319008 , 'name' : 'struct103' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4e2d159a51896cd88a'} , 'sdid' : 4869 , 'fsdid' : 83634 , 'name' : 'struct274' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4f2d159a51896ce99e'} , 'sdid' : 4869 , 'fsdid' : 872775 , 'name' : 'struct493' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d4f2d159a51896ceefe'} , 'sdid' : 4869 , 'fsdid' : 576602 , 'name' : 'struct418' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d502d159a51896cf0e8'} , 'sdid' : 4869 , 'fsdid' : 561804 , 'name' : 'struct16' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d502d159a51896cf246'} , 'sdid' : 4869 , 'fsdid' : 744009 , 'name' : 'struct257' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d502d159a51896cf875'} , 'sdid' : 4869 , 'fsdid' : 203057 , 'name' : 'struct177' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d502d159a51896d010a'} , 'sdid' : 4869 , 'fsdid' : 145113 , 'name' : 'struct363' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d512d159a51896d0d16'} , 'sdid' : 4869 , 'fsdid' : 117482 , 'name' : 'struct143' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d512d159a51896d1118'} , 'sdid' : 4869 , 'fsdid' : 469887 , 'name' : 'struct121' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d522d159a51896d21f2'} , 'sdid' : 4869 , 'fsdid' : 562487 , 'name' : 'struct465' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d532d159a51896d2dd5'} , 'sdid' : 4869 , 'fsdid' : 483845 , 'name' : 'struct317' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d552d159a51896d478d'} , 'sdid' : 4869 , 'fsdid' : 620479 , 'name' : 'struct117' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d552d159a51896d4d4a'} , 'sdid' : 4869 , 'fsdid' : 500835 , 'name' : 'struct145' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d562d159a51896d599c'} , 'sdid' : 4869 , 'fsdid' : 788636 , 'name' : 'struct170' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d562d159a51896d5aeb'} , 'sdid' : 4869 , 'fsdid' : 284207 , 'name' : 'struct37' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d582d159a51896d7f3a'} , 'sdid' : 4869 , 'fsdid' : 697548 , 'name' : 'struct206' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d582d159a51896d8543'} , 'sdid' : 4869 , 'fsdid' : 250456 , 'name' : 'struct426' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d592d159a51896d8c3d'} , 'sdid' : 4869 , 'fsdid' : 457787 , 'name' : 'struct109' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d592d159a51896d8f5b'} , 'sdid' : 4869 , 'fsdid' : 190916 , 'name' : 'struct339' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d592d159a51896d93b5'} , 'sdid' : 4869 , 'fsdid' : 843425 , 'name' : 'struct196' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d592d159a51896d9765'} , 'sdid' : 4869 , 'fsdid' : 548929 , 'name' : 'struct112' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5a2d159a51896da998'} , 'sdid' : 4869 , 'fsdid' : 524315 , 'name' : 'struct208' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5a2d159a51896dab3d'} , 'sdid' : 4869 , 'fsdid' : 612630 , 'name' : 'struct33' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5b2d159a51896db081'} , 'sdid' : 4869 , 'fsdid' : 882422 , 'name' : 'struct324' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5b2d159a51896db353'} , 'sdid' : 4869 , 'fsdid' : 759496 , 'name' : 'struct391' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5c2d159a51896dbea2'} , 'sdid' : 4869 , 'fsdid' : 401440 , 'name' : 'struct256' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5d2d159a51896dd013'} , 'sdid' : 4869 , 'fsdid' : 72838 , 'name' : 'struct306' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5d2d159a51896dd7c2'} , 'sdid' : 4869 , 'fsdid' : 921218 , 'name' : 'struct393' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5e2d159a51896deccc'} , 'sdid' : 4869 , 'fsdid' : 673326 , 'name' : 'struct264' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5f2d159a51896df6c9'} , 'sdid' : 4869 , 'fsdid' : 5733 , 'name' : 'struct421' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d5f2d159a51896dff62'} , 'sdid' : 4869 , 'fsdid' : 310146 , 'name' : 'struct440' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d602d159a51896e0a23'} , 'sdid' : 4869 , 'fsdid' : 984258 , 'name' : 'struct199' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d612d159a51896e1753'} , 'sdid' : 4869 , 'fsdid' : 253988 , 'name' : 'struct426' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d612d159a51896e1d14'} , 'sdid' : 4869 , 'fsdid' : 514296 , 'name' : 'struct493' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d622d159a51896e2b53'} , 'sdid' : 4869 , 'fsdid' : 747424 , 'name' : 'struct192' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d632d159a51896e410a'} , 'sdid' : 4869 , 'fsdid' : 884132 , 'name' : 'struct74' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d642d159a51896e48c7'} , 'sdid' : 4869 , 'fsdid' : 730343 , 'name' : 'struct204' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d642d159a51896e49b3'} , 'sdid' : 4869 , 'fsdid' : 679954 , 'name' : 'struct118' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d652d159a51896e620b'} , 'sdid' : 4869 , 'fsdid' : 788538 , 'name' : 'struct114' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d652d159a51896e637e'} , 'sdid' : 4869 , 'fsdid' : 487656 , 'name' : 'struct287' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d652d159a51896e67a6'} , 'sdid' : 4869 , 'fsdid' : 777955 , 'name' : 'struct442' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d662d159a51896e6d04'} , 'sdid' : 4869 , 'fsdid' : 333508 , 'name' : 'struct320' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d672d159a51896e82ba'} , 'sdid' : 4869 , 'fsdid' : 425312 , 'name' : 'struct206' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d672d159a51896e87f4'} , 'sdid' : 4869 , 'fsdid' : 789946 , 'name' : 'struct75' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d682d159a51896e95ea'} , 'sdid' : 4869 , 'fsdid' : 520850 , 'name' : 'struct63' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d682d159a51896e99f3'} , 'sdid' : 4869 , 'fsdid' : 541612 , 'name' : 'struct196' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d692d159a51896ea271'} , 'sdid' : 4869 , 'fsdid' : 346796 , 'name' : 'struct379' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d692d159a51896ea321'} , 'sdid' : 4869 , 'fsdid' : 729685 , 'name' : 'struct164' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}, { '_id' : { '$oid' : '4c9e1d692d159a51896eab71'} , 'sdid' : 4869 , 'fsdid' : 696785 , 'name' : 'struct122' , 'createTime' : 1285430557325 , 'type' : 1 , 'group' : 'helloqwerqwerqwer'}]}";
	ResponseMongodbPacket response = new ResponseMongodbPacket();
	BSONObject bs = null;
	{
		bs = (BSONObject)JSON.parse(string);
	}

	public MockClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		//this.setMessageHandler(new CommandMessageHandler(this));
	}

	protected synchronized void doReceiveMessage(byte[] message){
		response.documents = new ArrayList();
		response.documents.add(bs);
		response.numberReturned = 1;
		this.postMessage(response.toByteBuffer(this));
	}

}
