
// Description: Java 25 in-memory RAM DbIO implementation for License.

/*
 *	io.github.msobkow.CFInt
 *
 *	Copyright (c) 2016-2026 Mark Stephen Sobkow
 *	
 *	Mark's Code Fractal 3.1 CFInt - Internet Essentials
 *	
 *	This file is part of Mark's Code Fractal CFInt.
 *	
 *	Mark's Code Fractal CFInt is available under dual commercial license from
 *	Mark Stephen Sobkow, or under the terms of the GNU Library General Public License,
 *	Version 3 or later.
 *	
 *	Mark's Code Fractal CFInt is free software: you can redistribute it and/or
 *	modify it under the terms of the GNU Library General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Mark's Code Fractal CFInt is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU Library General Public License
 *	along with Mark's Code Fractal CFInt.  If not, see <https://www.gnu.org/licenses/>.
 *	
 *	If you wish to modify and use this code without publishing your changes in order to
 *	tie it to proprietary code, please contact Mark Stephen Sobkow
 *	for a commercial license at mark.sobkow@gmail.com
 *	
 */

package io.github.msobkow.v3_1.cfint.cfintram;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import io.github.msobkow.v3_1.cflib.*;
import io.github.msobkow.v3_1.cflib.dbutil.*;

import io.github.msobkow.v3_1.cfsec.cfsec.*;
import io.github.msobkow.v3_1.cfint.cfint.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;
import io.github.msobkow.v3_1.cfsec.cfsecobj.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;

/*
 *	CFIntRamLicenseTable in-memory RAM DbIO implementation
 *	for License.
 */
public class CFIntRamLicenseTable
	implements ICFIntLicenseTable
{
	private ICFIntSchema schema;
	private Map< CFIntLicensePKey,
				CFIntLicenseBuff > dictByPKey
		= new HashMap< CFIntLicensePKey,
				CFIntLicenseBuff >();
	private Map< CFIntLicenseByLicnTenantIdxKey,
				Map< CFIntLicensePKey,
					CFIntLicenseBuff >> dictByLicnTenantIdx
		= new HashMap< CFIntLicenseByLicnTenantIdxKey,
				Map< CFIntLicensePKey,
					CFIntLicenseBuff >>();
	private Map< CFIntLicenseByDomainIdxKey,
				Map< CFIntLicensePKey,
					CFIntLicenseBuff >> dictByDomainIdx
		= new HashMap< CFIntLicenseByDomainIdxKey,
				Map< CFIntLicensePKey,
					CFIntLicenseBuff >>();
	private Map< CFIntLicenseByUNameIdxKey,
			CFIntLicenseBuff > dictByUNameIdx
		= new HashMap< CFIntLicenseByUNameIdxKey,
			CFIntLicenseBuff >();

	public CFIntRamLicenseTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public void createLicense( CFSecAuthorization Authorization,
		CFIntLicenseBuff Buff )
	{
		final String S_ProcName = "createLicense";
		CFIntLicensePKey pkey = schema.getFactoryLicense().newPKey();
		pkey.setRequiredId( schema.nextLicenseIdGen() );
		Buff.setRequiredId( pkey.getRequiredId() );
		CFIntLicenseByLicnTenantIdxKey keyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		keyLicnTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntLicenseByDomainIdxKey keyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		keyDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntLicenseByUNameIdxKey keyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		keyUNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		keyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByUNameIdx.containsKey( keyUNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"LicenseUNameIdx",
				keyUNameIdx );
		}

		// Validate foreign keys

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Owner",
						"Owner",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopDomainId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFIntLicensePKey, CFIntLicenseBuff > subdictLicnTenantIdx;
		if( dictByLicnTenantIdx.containsKey( keyLicnTenantIdx ) ) {
			subdictLicnTenantIdx = dictByLicnTenantIdx.get( keyLicnTenantIdx );
		}
		else {
			subdictLicnTenantIdx = new HashMap< CFIntLicensePKey, CFIntLicenseBuff >();
			dictByLicnTenantIdx.put( keyLicnTenantIdx, subdictLicnTenantIdx );
		}
		subdictLicnTenantIdx.put( pkey, Buff );

		Map< CFIntLicensePKey, CFIntLicenseBuff > subdictDomainIdx;
		if( dictByDomainIdx.containsKey( keyDomainIdx ) ) {
			subdictDomainIdx = dictByDomainIdx.get( keyDomainIdx );
		}
		else {
			subdictDomainIdx = new HashMap< CFIntLicensePKey, CFIntLicenseBuff >();
			dictByDomainIdx.put( keyDomainIdx, subdictDomainIdx );
		}
		subdictDomainIdx.put( pkey, Buff );

		dictByUNameIdx.put( keyUNameIdx, Buff );

	}

	public CFIntLicenseBuff readDerived( CFSecAuthorization Authorization,
		CFIntLicensePKey PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readDerived";
		CFIntLicensePKey key = schema.getFactoryLicense().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntLicenseBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntLicenseBuff lockDerived( CFSecAuthorization Authorization,
		CFIntLicensePKey PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readDerived";
		CFIntLicensePKey key = schema.getFactoryLicense().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		CFIntLicenseBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntLicenseBuff[] readAllDerived( CFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamLicense.readAllDerived";
		CFIntLicenseBuff[] retList = new CFIntLicenseBuff[ dictByPKey.values().size() ];
		Iterator< CFIntLicenseBuff > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public CFIntLicenseBuff[] readDerivedByLicnTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByLicnTenantIdx";
		CFIntLicenseByLicnTenantIdxKey key = schema.getFactoryLicense().newLicnTenantIdxKey();
		key.setRequiredTenantId( TenantId );

		CFIntLicenseBuff[] recArray;
		if( dictByLicnTenantIdx.containsKey( key ) ) {
			Map< CFIntLicensePKey, CFIntLicenseBuff > subdictLicnTenantIdx
				= dictByLicnTenantIdx.get( key );
			recArray = new CFIntLicenseBuff[ subdictLicnTenantIdx.size() ];
			Iterator< CFIntLicenseBuff > iter = subdictLicnTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFIntLicensePKey, CFIntLicenseBuff > subdictLicnTenantIdx
				= new HashMap< CFIntLicensePKey, CFIntLicenseBuff >();
			dictByLicnTenantIdx.put( key, subdictLicnTenantIdx );
			recArray = new CFIntLicenseBuff[0];
		}
		return( recArray );
	}

	public CFIntLicenseBuff[] readDerivedByDomainIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByDomainIdx";
		CFIntLicenseByDomainIdxKey key = schema.getFactoryLicense().newDomainIdxKey();
		key.setRequiredTopDomainId( TopDomainId );

		CFIntLicenseBuff[] recArray;
		if( dictByDomainIdx.containsKey( key ) ) {
			Map< CFIntLicensePKey, CFIntLicenseBuff > subdictDomainIdx
				= dictByDomainIdx.get( key );
			recArray = new CFIntLicenseBuff[ subdictDomainIdx.size() ];
			Iterator< CFIntLicenseBuff > iter = subdictDomainIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFIntLicensePKey, CFIntLicenseBuff > subdictDomainIdx
				= new HashMap< CFIntLicensePKey, CFIntLicenseBuff >();
			dictByDomainIdx.put( key, subdictDomainIdx );
			recArray = new CFIntLicenseBuff[0];
		}
		return( recArray );
	}

	public CFIntLicenseBuff readDerivedByUNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByUNameIdx";
		CFIntLicenseByUNameIdxKey key = schema.getFactoryLicense().newUNameIdxKey();
		key.setRequiredTopDomainId( TopDomainId );
		key.setRequiredName( Name );

		CFIntLicenseBuff buff;
		if( dictByUNameIdx.containsKey( key ) ) {
			buff = dictByUNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntLicenseBuff readDerivedByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByIdIdx() ";
		CFIntLicensePKey key = schema.getFactoryLicense().newPKey();
		key.setRequiredId( Id );

		CFIntLicenseBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntLicenseBuff readBuff( CFSecAuthorization Authorization,
		CFIntLicensePKey PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readBuff";
		CFIntLicenseBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a110" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntLicenseBuff lockBuff( CFSecAuthorization Authorization,
		CFIntLicensePKey PKey )
	{
		final String S_ProcName = "lockBuff";
		CFIntLicenseBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a110" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntLicenseBuff[] readAllBuff( CFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamLicense.readAllBuff";
		CFIntLicenseBuff buff;
		ArrayList<CFIntLicenseBuff> filteredList = new ArrayList<CFIntLicenseBuff>();
		CFIntLicenseBuff[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new CFIntLicenseBuff[0] ) );
	}

	public CFIntLicenseBuff readBuffByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByIdIdx() ";
		CFIntLicenseBuff buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
			return( (CFIntLicenseBuff)buff );
		}
		else {
			return( null );
		}
	}

	public CFIntLicenseBuff[] readBuffByLicnTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByLicnTenantIdx() ";
		CFIntLicenseBuff buff;
		ArrayList<CFIntLicenseBuff> filteredList = new ArrayList<CFIntLicenseBuff>();
		CFIntLicenseBuff[] buffList = readDerivedByLicnTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
				filteredList.add( (CFIntLicenseBuff)buff );
			}
		}
		return( filteredList.toArray( new CFIntLicenseBuff[0] ) );
	}

	public CFIntLicenseBuff[] readBuffByDomainIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByDomainIdx() ";
		CFIntLicenseBuff buff;
		ArrayList<CFIntLicenseBuff> filteredList = new ArrayList<CFIntLicenseBuff>();
		CFIntLicenseBuff[] buffList = readDerivedByDomainIdx( Authorization,
			TopDomainId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
				filteredList.add( (CFIntLicenseBuff)buff );
			}
		}
		return( filteredList.toArray( new CFIntLicenseBuff[0] ) );
	}

	public CFIntLicenseBuff readBuffByUNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByUNameIdx() ";
		CFIntLicenseBuff buff = readDerivedByUNameIdx( Authorization,
			TopDomainId,
			Name );
		if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
			return( (CFIntLicenseBuff)buff );
		}
		else {
			return( null );
		}
	}

	public void updateLicense( CFSecAuthorization Authorization,
		CFIntLicenseBuff Buff )
	{
		CFIntLicensePKey pkey = schema.getFactoryLicense().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntLicenseBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateLicense",
				"Existing record not found",
				"License",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateLicense",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntLicenseByLicnTenantIdxKey existingKeyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		existingKeyLicnTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntLicenseByLicnTenantIdxKey newKeyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		newKeyLicnTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntLicenseByDomainIdxKey existingKeyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		existingKeyDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntLicenseByDomainIdxKey newKeyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		newKeyDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntLicenseByUNameIdxKey existingKeyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		existingKeyUNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		existingKeyUNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntLicenseByUNameIdxKey newKeyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		newKeyUNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		newKeyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyUNameIdx.equals( newKeyUNameIdx ) ) {
			if( dictByUNameIdx.containsKey( newKeyUNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateLicense",
					"LicenseUNameIdx",
					newKeyUNameIdx );
			}
		}

		// Validate foreign keys

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateLicense",
						"Owner",
						"Owner",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopDomainId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateLicense",
						"Container",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Update is valid

		Map< CFIntLicensePKey, CFIntLicenseBuff > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		subdict = dictByLicnTenantIdx.get( existingKeyLicnTenantIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByLicnTenantIdx.containsKey( newKeyLicnTenantIdx ) ) {
			subdict = dictByLicnTenantIdx.get( newKeyLicnTenantIdx );
		}
		else {
			subdict = new HashMap< CFIntLicensePKey, CFIntLicenseBuff >();
			dictByLicnTenantIdx.put( newKeyLicnTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByDomainIdx.get( existingKeyDomainIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByDomainIdx.containsKey( newKeyDomainIdx ) ) {
			subdict = dictByDomainIdx.get( newKeyDomainIdx );
		}
		else {
			subdict = new HashMap< CFIntLicensePKey, CFIntLicenseBuff >();
			dictByDomainIdx.put( newKeyDomainIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByUNameIdx.remove( existingKeyUNameIdx );
		dictByUNameIdx.put( newKeyUNameIdx, Buff );

	}

	public void deleteLicense( CFSecAuthorization Authorization,
		CFIntLicenseBuff Buff )
	{
		final String S_ProcName = "CFIntRamLicenseTable.deleteLicense() ";
		String classCode;
		CFIntLicensePKey pkey = schema.getFactoryLicense().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		CFIntLicenseBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteLicense",
				pkey );
		}
		CFIntLicenseByLicnTenantIdxKey keyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		keyLicnTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntLicenseByDomainIdxKey keyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		keyDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntLicenseByUNameIdxKey keyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		keyUNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		keyUNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFIntLicensePKey, CFIntLicenseBuff > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByLicnTenantIdx.get( keyLicnTenantIdx );
		subdict.remove( pkey );

		subdict = dictByDomainIdx.get( keyDomainIdx );
		subdict.remove( pkey );

		dictByUNameIdx.remove( keyUNameIdx );

	}
	public void deleteLicenseByIdIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argId )
	{
		CFIntLicensePKey key = schema.getFactoryLicense().newPKey();
		key.setRequiredId( argId );
		deleteLicenseByIdIdx( Authorization, key );
	}

	public void deleteLicenseByIdIdx( CFSecAuthorization Authorization,
		CFIntLicensePKey argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntLicenseBuff cur;
		LinkedList<CFIntLicenseBuff> matchSet = new LinkedList<CFIntLicenseBuff>();
		Iterator<CFIntLicenseBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntLicenseBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}

	public void deleteLicenseByLicnTenantIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntLicenseByLicnTenantIdxKey key = schema.getFactoryLicense().newLicnTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteLicenseByLicnTenantIdx( Authorization, key );
	}

	public void deleteLicenseByLicnTenantIdx( CFSecAuthorization Authorization,
		CFIntLicenseByLicnTenantIdxKey argKey )
	{
		CFIntLicenseBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntLicenseBuff> matchSet = new LinkedList<CFIntLicenseBuff>();
		Iterator<CFIntLicenseBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntLicenseBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}

	public void deleteLicenseByDomainIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId )
	{
		CFIntLicenseByDomainIdxKey key = schema.getFactoryLicense().newDomainIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		deleteLicenseByDomainIdx( Authorization, key );
	}

	public void deleteLicenseByDomainIdx( CFSecAuthorization Authorization,
		CFIntLicenseByDomainIdxKey argKey )
	{
		CFIntLicenseBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntLicenseBuff> matchSet = new LinkedList<CFIntLicenseBuff>();
		Iterator<CFIntLicenseBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntLicenseBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}

	public void deleteLicenseByUNameIdx( CFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId,
		String argName )
	{
		CFIntLicenseByUNameIdxKey key = schema.getFactoryLicense().newUNameIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		key.setRequiredName( argName );
		deleteLicenseByUNameIdx( Authorization, key );
	}

	public void deleteLicenseByUNameIdx( CFSecAuthorization Authorization,
		CFIntLicenseByUNameIdxKey argKey )
	{
		CFIntLicenseBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntLicenseBuff> matchSet = new LinkedList<CFIntLicenseBuff>();
		Iterator<CFIntLicenseBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntLicenseBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}
}
